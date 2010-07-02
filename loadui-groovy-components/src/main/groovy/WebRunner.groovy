// 
// Copyright 2010 eviware software ab
// 
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
// 
// http://ec.europa.eu/idabc/eupl5
// 
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
// 

/**
 * Sends a HTTP request. Either set the URL via the 'url' property, or trigger 
 * the Sampler with a message containing the parameter 'url'.
 * 
 * @help http://www.loadui.org/Samplers/web-page-runner.html
 * @name Web Page Runner
 * @category runners
 * @dependency org.apache.httpcomponents:httpclient:4.1-alpha2
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
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.util.layout.DelayedFormattedString
import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.impl.component.categories.SamplerBase.SampleCancelledException

import java.util.HashSet
import java.util.Collections

sr = new SchemeRegistry()
sr.register( new Scheme( "http", PlainSocketFactory.socketFactory, 80 ) )

cm = new ThreadSafeClientConnManager( sr )
cm.maxTotalConnections = 50000
cm.defaultMaxPerRoute = 50000
http = new DefaultHttpClient( cm )

def runningSamples = Collections.synchronizedSet( new HashSet() )
runAction = null

//Properties
createProperty( 'url', String )
createProperty( 'outputBody', Boolean, false )

createProperty( 'propagateSession', Boolean, false )
createProperty( 'readResponse', Boolean, false )
createProperty( 'raiseAssertion', Boolean, false )
createProperty( 'errorCodeList', String )

createProperty( 'proxyHost', String)
createProperty( 'proxyPort', Long)
createProperty( 'proxyUsername', String)
createProperty( 'proxyPassword', String)

eviPattern = ~/https?:\/\/(www\.)?(eviware\.com|(soapui|loadui)\.org)(\/.*)?/
dummyUrl = "http://GoSpamYourself.com"

validateUrl = {  
	if ((url.value != null) && !(url.value.toLowerCase().startsWith( "http://" ) || url.value.toLowerCase().startsWith( "https://" ))) {
		url.value = "http://" + url.value;
	}
	
	if( url.value != null && eviPattern.matcher(url.value).matches() ) url.value = dummyUrl
	
	setInvalid( url.value == null || url.value == dummyUrl )
	runAction?.enabled = !isInvalid()
}

updateProxy = {
	if( proxyHost.value != null && proxyHost.value.trim().length() > 0 && proxyPort.value != null && proxyPort.value > 0 ) {
		HttpHost hcProxyHost = new HttpHost(proxyHost.value, (int)proxyPort.value, "http");
		http.params.setParameter(ConnRoutePNames.DEFAULT_PROXY, hcProxyHost);
		
		if( proxyUsername.value != null && proxyUsername.value.trim().length() > 0 && proxyPassword.value != null  ) {
			http.credentialsProvider.setCredentials( 
					new AuthScope(proxyHost.value, (int)proxyPort.value), 
					new UsernamePasswordCredentials(proxyUsername.value, 
					new String(proxyPassword.value)))
		}
		else {
			http.credentialsProvider = null
		}
	}
	else {
		http.params.setParameter(ConnRoutePNames.DEFAULT_PROXY, null );
	}
}

validateUrl()
updateProxy()

sampleResetValue = 0
discardResetValue = 0
failedResetValue = 0
aborting = false

displayRunning = new DelayedFormattedString( '%d', 500, value { currentlyRunning } )
displayTotal = new DelayedFormattedString( '%d', 500,  value { sampleCounter.get() - sampleResetValue } )
displayQueue = new DelayedFormattedString( '%d', 500, value { queueSize } )
displayDiscarded = new DelayedFormattedString( '%d', 500,  value { discardCounter.get() - discardResetValue } )
displayFailed = new DelayedFormattedString( '%d', 500,  value { failureCounter.get() - failedResetValue } )

sample = { message, sampleId ->
	def uri = message['url'] ?: url.value
	if( uri ) {
		def get = new HttpGet( uri )
		
		
		runningSamples.add( get )
		try {
			def response = http.execute( get )
			message['Status'] = true
			message['URI'] = uri
			message['HttpStatus'] = response.statusLine.statusCode
			
			if (errorCodeList.value != null) {
				def assertionCodes = errorCodeList.value.split(',')
				
				for (code in assertionCodes) {
					if (code.trim() == response.statusLine.statusCode.toString()) {
						failureCounter.increment();
						break;
					}
				}
			}
			
			if( response.entity != null )	{
				int contentLength = response.entity.contentLength
				message['Bytes'] = contentLength
				
				if( outputBody.value )
					message['Response'] = EntityUtils.toString(response.entity)
				
				if( contentLength < 0 ) {
					if( outputBody.value)
						message['Bytes'] = message['Response'].length()
					else
						message['Bytes'] = EntityUtils.toString(response.entity).length()
				}
				
				response.entity.consumeContent()
				
				if (!runningSamples.remove(get)) {
					throw new SampleCancelledException()
				}
				
				return message
			}
		} catch( e ) {
			if( e instanceof SampleCancelledException )
				throw e;
			
			e.printStackTrace()
			get.abort()
			
			if (!runningSamples.remove(get)) {
				throw new SampleCancelledException()
			}
			
			message['Status'] = false
			if (raiseAssertion.value == true) {
				failureCounter.increment()
			}
			
			return message
		}
	} else {
		throw new SampleCancelledException()
	}
}

onCancel = {
	aborting = true
	
	def methods = runningSamples.toArray()
	methods.each{  method ->
		if( !method.aborted ) 
			method.abort()
	}
	runningSamples = []
	aborting = false
}

onRelease = {
	displayRunning.release()
	displayTotal.release()
	displayQueue.release()
	displayDiscarded.release()
	displayFailed.release()
}

addEventListener( ActionEvent ) { event ->
	if ( event.key == "RESET" ) {
		sampleResetValue = 0
		discardResetValue = 0
		failedResetValue = 0
	}
	else if ( event.key == "BROWSE" ) {
		if( url.value != null && url.value.startsWith( "http"))
			java.awt.Desktop.getDesktop().browse( new java.net.URI(url.value ))
	}
}

addEventListener( PropertyEvent ) { event ->
	if ( event.event == PropertyEvent.Event.VALUE ) {
		if( event.property == url ) {
			validateUrl()
		}
		else if( event.property == proxyHost || event.property == proxyPort ||
		event.property == proxyUsername || event.property == proxyPassword ) {
			updateProxy()
		}
	}
}

//Layout
layout( constraints: 'gap 10 0') {
	box (  ){
		property( property:url, label:'Web Page Address', constraints: 'w 300!, spanx 2, wrap', style: '-fx-font-size: 17pt' )
		action( label:'Open in Browser', constraints:'spanx 2,wrap', action: { triggerAction('BROWSE') } )
		runAction = action( label:'Run Once', action: { triggerAction('SAMPLE') } )
		action( label:'Abort Running Pages', action: { triggerAction('CANCEL') } )
	}
	
	separator(vertical:true)
	box( constraints:'wrap 1'){
		box( widget:'display', constraints:'wrap 3, w 180!, align right' ) {
			node( label:'Running', fString:displayRunning, constraints:'w 50!' )
			node( label:'Samples', fString:displayTotal, constraints:'w 60!' )
			node( label:'Failed', fString:displayFailed, constraints:'w 50!' )
			node( label:'Queue', fString:displayQueue, constraints:'w 50!' )
			node( label:'Discarded', fString:displayDiscarded, constraints:'w 60!' )
		}
		action( label:'Reset', action: {
			sampleResetValue = sampleCounter.get()
			discardResetValue = discardCounter.get()
			failedResetValue = failureCounter.get()
		}, constraints:'align right' )
	}
}


settings( label: "Basic" ) {
	property( property: outputBody, label: 'Output Response Body' )
	property( property: propagateSession, label: 'Propagate Session' )
	property( property: readResponse, label: 'Read Response' )
	property( property: raiseAssertion, label: 'Raise Assertion on Error' )
	property( property: concurrentSamples, label: 'Max Concurrent Samples' )
	property( property: maxQueueSize, label: 'Max Queue' )
	property( property: assertOnOverflow, label: 'Raise Assertion on Overflow' )
	property( property: errorCodeList, label: 'Error Codes that Raise an Assertion', constraints:'w 200!')
}

settings( label: "Proxy" ) {
	property( property: proxyHost, label: 'Proxy Host' )
	property( property: proxyPort, label: 'Proxy Port' )
	property( property: proxyUsername, label: 'Proxy Username' )
	property( property: proxyPassword, label: 'Proxy Password' )
}