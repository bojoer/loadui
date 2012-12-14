/**
 * Fetches a web page.
 * 
 * @id com.eviware.HtmlRunner
 * @name HTML Runner
 * @category runners
 * @dependency org.apache.httpcomponents:httpcore:4.1
 * @dependency org.apache.httpcomponents:httpclient:4.1.1
 * @dependency net.sourceforge.htmlunit:htmlunit:2.9
 */

import org.apache.http.* 
import org.apache.http.client.*
import org.apache.http.auth.*
import org.apache.http.conn.params.*
import org.apache.http.conn.scheme.*
import org.apache.http.impl.client.BasicCredentialsProvider
import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.impl.component.categories.RunnerBase.SampleCancelledException

import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.KeyManager
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import java.security.cert.CertificateException
import java.security.SecureRandom
import java.util.HashMap
import java.util.concurrent.TimeUnit

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.WebRequestSettings

//SSL support, trust all certificates and hostnames.
class NaiveTrustManager implements X509TrustManager {
	void checkClientTrusted ( X509Certificate[] cert, String authType ) throws CertificateException {}
	void checkServerTrusted ( X509Certificate[] cert, String authType ) throws CertificateException {}
	X509Certificate[] getAcceptedIssuers () { null }
}
def sslContext = SSLContext.getInstance( 'SSL' )
TrustManager[] tms = [ new NaiveTrustManager() ]
sslContext.init( new KeyManager[0], tms, new SecureRandom() )
def sslSocketFactory = new SSLSocketFactory( sslContext )
sslSocketFactory.hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER

def sr = new SchemeRegistry()
sr.register( new Scheme( "http", PlainSocketFactory.socketFactory, 80 ) )
sr.register( new Scheme( "https", sslSocketFactory, 443 ) )

//Properties
createProperty( 'url', String ) { ->
	validateUrl()
}
createProperty( 'outputBody', Boolean, false )
createProperty( 'downloadResources', Boolean, true )
createProperty( 'runJavaScript', Boolean, true )
createProperty( 'errorCodeList', String )

authUsername = createProperty( '_authUsername', String )
authPassword = createProperty( '_authPassword', String )

inlineUrlAuthUsername = null
inlineUrlAuthPassword = null
credentialsProvider = new BasicCredentialsProvider()
			
def runningSamples = ( [] as Set ).asSynchronized()
runAction = null

def dummyUrl = "http://GoSpamYourself.com"

validateUrl = {
	if( url.value && !( url.value.toLowerCase().startsWith( "http://" ) || url.value.toLowerCase().startsWith( "https://" ) ) ) {
		url.value = "http://" + url.value
	}
	
	if( url.value =~ /https?:\/\/(www\.)?(eviware\.com|(soapui|loadui)\.org)(\/.*)?/ ) url.value = dummyUrl
	
	// extract possible username and password from username:password@domain syntax
	matcher = url.value?.replace( "http://", "" ) =~ /([^:]+):([^@]+)@(.+)/
	if ( matcher ) {
		inlineUrlAuthUsername = matcher[0][1]
		inlineUrlAuthPassword = matcher[0][2]
	} else {
		inlineUrlAuthUsername = inlineUrlAuthPassword = null
	}
	updateAuth()
	
	try {
		new URI( url.value )
		setInvalid( !url.value || url.value == dummyUrl )
	} catch( e ) {
		setInvalid( true )
	}
	
	runAction?.enabled = !isInvalid()
}

updateAuth = {
	def username = null
	def password = null
	if( inlineUrlAuthUsername && inlineUrlAuthPassword ) {
		username = inlineUrlAuthUsername
		password = inlineUrlAuthPassword
	} else if( authUsername.value?.trim() && authPassword.value?.trim() ) {
		username = authUsername.value
		password = authPassword.value
	}
	
	if( username && password ) {
		credentialsProvider.setCredentials(
			new AuthScope( AuthScope.ANY ), 
			new UsernamePasswordCredentials( username, password )
		)
	}
}

validateUrl()

requestResetValue = 0
sampleResetValue = 0
discardResetValue = 0
failedResetValue = 0


acceptTypes = new HashMap<String, String>()
acceptTypes.put("html", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
acceptTypes.put("img", "image/png,image/*;q=0.8,*/*;q=0.5")
acceptTypes.put("script", "*/*")
acceptTypes.put("style", "text/css,*/*;q=0.1")

def downloadCssAndImages( page ) {
	def bytesDownloaded = 0
	def xPathExpression = "//*[name() = 'img' or name() = 'link' and @type = 'text/css']"
	def resultList = page.getByXPath(xPathExpression)
	resultList.each {
		try {
			def path = it.getAttribute( 'src' ).equals( '' ) ? it.getAttribute( 'href' ) : it.getAttribute( 'src' )
			if ( path != null && !path.equals( '' ) ) {

				def url = page.getFullyQualifiedUrl(path)
				def wrs = new WebRequestSettings(url)
				wrs.setAdditionalHeader( 'Referer', page.webResponse.requestSettings.url.toString() )

				client.addRequestHeader( 'Accept', acceptTypes[ it.tagName.toLowerCase() ] )
				bytesDownloaded += client.getPage( wrs ).webResponse.contentAsString.length()
			}
		} catch ( e ) { log.warn("Error getting resource: $e") }
	}

	client.removeRequestHeader( 'Accept' )
	return bytesDownloaded
}

sample = { message, sampleId ->

	def uri = message['url'] ?: url.value
	if( uri ) {
		message['ID'] = uri
		
		client = new WebClient()
		client.setJavaScriptEnabled( runJavaScript.value )
		runningSamples.add( client )
		try {
			//client.setCredentialsProvider( credentialsProvider )
			def page = client.getPage( uri )
			
			def bytesDownloaded = 0
			if( downloadResources.value )
				bytesDownloaded = downloadCssAndImages( page )
		
			//def response = http.execute( get )
			message['Status'] = true
			message['URI'] = uri
			def statusCode = page.webResponse.statusCode
			message['HttpStatus'] = statusCode
			
			if( errorCodeList.value ) {
				def assertionCodes = errorCodeList.value.split(',')*.trim()
				if( assertionCodes.contains( statusCode.toString() ) )
				{
					failedRequestCounter.increment()
					failureCounter.increment()
				}
			}
			
			if( true /* response.entity != null */ )	{
				message['Bytes'] = page.webResponse.contentAsString.length() + bytesDownloaded
				
				if( outputBody.value )
					message['Response'] = page.webResponse.contentAsString()
				
				if( !runningSamples.remove( client ) ) {
					throw new SampleCancelledException()
				}
				
				return message
			}
		} catch( e ) {
			if( e instanceof SampleCancelledException )
				throw e
			
			if( e instanceof IOException )
				log.warn( "IOException in {}: {}", label, e.message )
			else
				log.error( "Exception in $label:", e )
			
			if ( !runningSamples.remove( client ) ) {
				throw new SampleCancelledException()
			}
			
			message['Status'] = false
			failedRequestCounter.increment()
			failureCounter.increment()
			
			return message
		}
	} else {
		throw new SampleCancelledException()
	}

}

/*
onCancel = {
	def numberOfRunning = 0
	synchronized( runningSamples ) {
		def methods = runningSamples.toArray()
		numberOfRunning = methods.size()
		runningSamples.clear()
		methods.each { if( !it.aborted ) it.abort() }
	}
	
	return numberOfRunning
}*/

onAction( 'RESET' ) {
	requestResetValue = 0
	sampleResetValue = 0
	discardResetValue = 0
	failedResetValue = 0
}

addEventListener( PropertyEvent ) { event ->
	if ( event.event == PropertyEvent.Event.VALUE ) {
		if( event.property in [ authUsername, authPassword ] ) {
			credentialsProvider.clear()
			updateAuth()
		}
	}
}

//Layout
layout {
	box( layout:'wrap 2, ins 0' ) {
		property( property:url, label:'Web Page Address', constraints: 'w 270!, spanx 2' )
		
		separator()
		
		property( property:runJavaScript, label:'Enable JavaScript', constraints: 'w 110!, spanx 1' )
		property( property:downloadResources, label:'Download images and CSS', constraints: 'w 150!, spanx 1' )
		
		runAction = action( label:'Run Once', action: { triggerAction( 'SAMPLE' ) } )
		action( label:'Abort Running Pages', action: { triggerAction( 'CANCEL' ) } )
	}
	separator(vertical:true)
	box( layout:'wrap, ins 0' ){
		box( widget:'display', layout:'wrap 3, align right' ) {
			node( label:'Requests', content: { requestCounter.get() - requestResetValue }, constraints:'w 50!' )
			node( label:'Running', content: { currentlyRunning }, constraints:'w 50!' )
			node( label:'Completed', content: { sampleCounter.get() - sampleResetValue }, constraints:'w 60!' )
			node( label:'Queued', content: { queueSize }, constraints:'w 50!' )
			node( label:'Discarded', content: { discardCounter.get() - discardResetValue }, constraints:'w 50!' )
			node( label:'Failed', content: { failureCounter.get() - failedResetValue }, constraints:'w 60!' )
		}
		action( label:'Reset', action: {
			requestResetValue = requestCounter.get()
			sampleResetValue = sampleCounter.get()
			discardResetValue = discardCounter.get()
			failedResetValue = failureCounter.get()
			triggerAction( 'CANCEL' )
		}, constraints:'align right' )
	}
}

//Compact Layout
compactLayout {
	box( widget:'display', layout:'wrap 3, align right' ) {
		node( label:'Requests', content: { requestCounter.get() - requestResetValue }, constraints:'w 50!' )
		node( label:'Running', content: { currentlyRunning }, constraints:'w 50!' )
		node( label:'Completed', content: { sampleCounter.get() - sampleResetValue }, constraints:'w 60!' )
		node( label:'Queued', content: { queueSize }, constraints:'w 50!' )
		node( label:'Discarded', content: { discardCounter.get() - discardResetValue }, constraints:'w 50!' )
		node( label:'Failed', content: { failureCounter.get() - failedResetValue }, constraints:'w 60!' )
	}
}

settings( label: 'Basic' ) {
	property( property: outputBody, label: 'Output Response Body' )
	property( property: concurrentSamples, label: 'Max Concurrent Requests' )
	property( property: maxQueueSize, label: 'Max Queue' )
	property( property: errorCodeList, label: 'Error Codes that Count as Failures', constraints:'w 200!')
	property( property: countDiscarded, label: 'Count Discarded Requests as Failed' )
}

settings( label: 'Authentication' ) {
	property( property: authUsername, label: 'Username' )
	property( property: authPassword, widget: 'password', label: 'Password' )
}
