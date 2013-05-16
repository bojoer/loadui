// 
// Copyright 2013 SmartBear Software
// 
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
// 
// http://ec.europa.eu/idabc/eupl
// 
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
// 

/**
 * Sends an HTTP request
 * 
 * @id com.eviware.WebRunner
 * @help http://www.loadui.org/Runners/web-page-runner-component.html
 * @name Web Page Runner
 * @category runners
 * @dependency org.apache.httpcomponents:httpcore:4.1.1
 * @dependency org.apache.httpcomponents:httpclient:4.1.1
 */

import org.apache.http.* 
import org.apache.http.client.*
import org.apache.http.auth.*
import org.apache.http.conn.params.*
import org.apache.http.conn.scheme.*
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.util.EntityUtils
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.impl.component.categories.RunnerBase.SampleCancelledException
import com.eviware.loadui.impl.component.ActivityStrategies
import com.eviware.loadui.util.ReleasableUtils

import java.util.concurrent.TimeUnit

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
import java.util.Map
import java.util.concurrent.TimeUnit

import org.apache.http.conn.ssl.X509HostnameVerifier
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSession

//SSL support, trust all certificates and hostnames.
class NaiveTrustManager implements X509TrustManager {
	void checkClientTrusted ( X509Certificate[] cert, String authType ) throws CertificateException {}
	void checkServerTrusted ( X509Certificate[] cert, String authType ) throws CertificateException {}
	X509Certificate[] getAcceptedIssuers () { null }
}

class AllowAllHostNamesVerifier implements X509HostnameVerifier {
	void verify(String host, SSLSocket ssl) throws IOException {}
	void verify(String host, X509Certificate cert) throws SSLException {}
	void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {}
	boolean verify(String hostname, SSLSession session) {}
}

def sslContext = SSLContext.getInstance("SSL")
TrustManager[] tms = [ new NaiveTrustManager() ]
sslContext.init( new KeyManager[0], tms, new SecureRandom() )

def sslSocketFactory = new SSLSocketFactory( sslContext, new AllowAllHostNamesVerifier() )

def sr = new SchemeRegistry()
sr.register( new Scheme( "http", PlainSocketFactory.socketFactory, 80 ) )
sr.register( new Scheme( "https", sslSocketFactory, 443 ) )

def cm = new ThreadSafeClientConnManager( sr )
cm.maxTotal = 50000
cm.defaultMaxPerRoute = 50000


//Properties
createProperty( 'url', String ) { ->
	validateUrl()
}
createProperty( 'outputBody', Boolean, false )

createProperty( 'errorCodeList', String )

createProperty( 'proxyHost', String )
createProperty( 'proxyPort', Long )
createProperty( 'proxyUsername', String )
proxyPassword = createProperty( '_proxyPassword', String )
authUsername = createProperty( '_authUsername', String )
authPassword = createProperty( '_authPassword', String )

http = new DefaultHttpClient( cm )

inlineUrlAuthUsername = null
inlineUrlAuthPassword = null
			
def runningSamples = ([] as Set).asSynchronized()
runAction = null

def dummyUrl = "http://GoSpamYourself.com"
def validUrl = ""
validateUrl = {
	def cleanUrl = url.value
	if( !(cleanUrl ==~ "https?://.*") ) {
		cleanUrl = 'http://'+cleanUrl
	}
	
	if( cleanUrl =~ /https?:\/\/(www\.)?(eviware\.com|(soapui|loadui)\.org)(\/.*)?/ ) {
		url.value = dummyUrl
		setInvalid( true )
		return
	}
	
	// extract possible username and password from username:password@domain syntax
	matcher = cleanUrl?.replace( "http://", "" ) =~ /([^:]+):([^@]+)@(.+)/
	if ( matcher ) {
		inlineUrlAuthUsername = matcher[0][1]
		inlineUrlAuthPassword = matcher[0][2]
	} else {
		inlineUrlAuthUsername = inlineUrlAuthPassword = null
	}
	updateAuth()
	
	try {
		new URI( cleanUrl )
		setInvalid( !url.value || url.value == dummyUrl )
	} catch( e ) {
		setInvalid( true )
	}
	
	runAction?.enabled = !isInvalid()
	validUrl = cleanUrl
}

updateProxy = {
	if( proxyHost.value?.trim() && proxyPort.value ) {
		HttpHost hcProxyHost = new HttpHost( proxyHost.value, (int)proxyPort.value, "http" )
		http.params.setParameter( ConnRoutePNames.DEFAULT_PROXY, hcProxyHost )
		
		if( proxyUsername.value?.trim() && proxyPassword.value ) {
			http.credentialsProvider.setCredentials(
				new AuthScope( proxyHost.value, (int)proxyPort.value ), 
				new UsernamePasswordCredentials( proxyUsername.value, proxyPassword.value )
			)
		} else {
			http.credentialsProvider.clear()
		}
	} else {
		http.params.setParameter( ConnRoutePNames.DEFAULT_PROXY, null )
	}
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
		http.credentialsProvider.setCredentials(
			new AuthScope( AuthScope.ANY ), 
			new UsernamePasswordCredentials( username, password )
		)
	}
}

validateUrl()
updateProxy()

requestResetValue = 0
sampleResetValue = 0
discardResetValue = 0
failedResetValue = 0

sample = { message, sampleId ->
	def uri = message['url'] ?: validUrl
	if( uri ) {
		def get = new HttpGet( uri )
		message['ID'] = uri
		
		runningSamples.add( get )
		try {
			def response = http.execute( get )
			message['Status'] = true
			message['URI'] = uri
			message['HttpStatus'] = response.statusLine.statusCode
			
			if( errorCodeList.value ) {
				def assertionCodes = errorCodeList.value.split(',')
				
				for( code in assertionCodes ) {
					if( code.trim() == response.statusLine.statusCode.toString() ) {
						failedRequestCounter.increment()
						failureCounter.increment()
						break
					}
				}
			}
			
			if( response.entity != null )	{
				int contentLength = response.entity.contentLength
				message['Bytes'] = contentLength
				
				if( outputBody.value )
					message['Response'] = EntityUtils.toString( response.entity )
				
				if( contentLength < 0 ) {
					if( outputBody.value )
						message['Bytes'] = message['Response'].length()
					else
						message['Bytes'] = EntityUtils.toString( response.entity ).length()
				}
				
				response.entity.consumeContent()
				
				if( !runningSamples.remove( get ) ) {
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
			
			get.abort()
			
			if ( !runningSamples.remove( get ) ) {
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

onCancel = {
	def numberOfRunning = 0
	synchronized( runningSamples ) {
		def methods = runningSamples.toArray()
		numberOfRunning = methods.size()
		runningSamples.clear()
		methods.each { if( !it.aborted ) it.abort() }
	}
	
	return numberOfRunning
}

onAction( "RESET" ) {
	requestResetValue = 0
	sampleResetValue = 0
	discardResetValue = 0
	failedResetValue = 0
}

addEventListener( PropertyEvent ) { event ->
	if ( event.event == PropertyEvent.Event.VALUE ) {
		if( event.property in [ proxyHost, proxyPort, proxyUsername, proxyPassword, authUsername, authPassword ] ) {
			http.credentialsProvider.clear()
			updateProxy()
			updateAuth()
		}
	}
}

//Layout
layout {
	
	box( layout:'wrap 2, ins 0' ) {
		property( property:url, label:'Web Page Address', constraints: 'w 300!, spanx 2', style: '-fx-font-size: 17pt' )
		action( label:'Open in Browser', constraints:'spanx 2', action: {
			java.awt.Desktop.desktop.browse( new java.net.URI( validUrl ) )
		} )
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
			triggerAction('CANCEL')
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

settings( label: "Basic" ) {
	property( property: outputBody, label: 'Output Response Body' )
	//property( property: propagateSession, label: 'Propagate Session' )
	property( property: concurrentSamples, label: 'Max Concurrent Requests' )
	property( property: maxQueueSize, label: 'Max Queue' )
	property( property: errorCodeList, label: 'Error Codes that Count as Failures', constraints:'w 200!')
	property( property: countDiscarded, label: 'Count Discarded Requests as Failed' )
	
}

settings( label: "Authentication" ) {
	property( property: authUsername, label: 'Username' )
	property( property: authPassword, widget: 'password', label: 'Password' )
}

settings( label: "Proxy" ) {
	property( property: proxyHost, label: 'Proxy Host' )
	property( property: proxyPort, label: 'Proxy Port' )
	property( property: proxyUsername, label: 'Proxy Username' )
	property( property: proxyPassword, widget: 'password', label: 'Proxy Password' )
}