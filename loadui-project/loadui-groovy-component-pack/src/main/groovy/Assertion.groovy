// 
// Copyright 2011 SmartBear Software
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
 * Checks for errors and increases global assertion count
 * 
 * @id com.eviware.Assertion
 * @help http://www.loadui.org/Analysis/assertion-component.html
 * @category analysis
 * @nonBlocking true
 * @deprecated
 */

import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.impl.layout.OptionsProviderImpl
import com.eviware.loadui.util.statistics.CounterStatisticSupport
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import com.eviware.loadui.util.ReleasableUtils

//Here to support Assertion components created in loadUI 1.0, remove in the future:
try { renameProperty( 'value', 'valueName' ) } catch( e ) {}

createOutput( 'output', 'Failed messages', 'Messages that did not pass the assertion are outputted here.' )
inputTerminal.label = 'Messages to assert'
inputTerminal.description = 'Messages sent here will be verified based on their values.'
likes( inputTerminal ) { it.messageSignature.values().any { Number.isAssignableFrom( it ) } }

def componentSignature = [
		"Assert" : String.class,
		"Min" : Long.class,
		"Value" : Long.class,
		"Max" : Long.class,
		"Period" : Long.class,
		"Tolerance" : Long.class,
		"Timestamp" : Long.class
		]
setSignature(output, componentSignature)

failureCounter = counters[CanvasItem.FAILURE_COUNTER]
assertionFailureCounter = counters[CanvasItem.ASSERTION_FAILURE_COUNTER]
totalCounter = counters[CanvasItem.ASSERTION_COUNTER]

// Expose statistics
counterStatisticSupport = new CounterStatisticSupport( component )
assertionFailuresVariable = addStatisticVariable( "Assertion Failures", "COUNTER" )
counterStatisticSupport.addCounterVariable( CanvasItem.ASSERTION_FAILURE_COUNTER, assertionFailuresVariable )
counterStatisticSupport.init()

//Properties
createProperty( 'valueName', String, "Select value" ) { value ->
	valueToAssert = value
	resetComponent()
}
createProperty( 'max', Long, 1000 )
createProperty( 'min', Long, 0 ) { value ->
	if(max.value < value ) {
		max.value = value
	}
}
createProperty( 'tolerance', Long, 1 )
createProperty( 'period', Long, 0 )

createProperty( 'sampleId', String, "" )
createProperty( 'failOnMissingID', Boolean, false )
createProperty( 'failOnMissingValue', Boolean, false )
createProperty( 'includeAssertedMessage', Boolean, false )

buffer = [] as LinkedList

outMsg = newMessage()

OptionsProviderImpl provider = new OptionsProviderImpl([])

assertedResetValue = 0
failedResetValue = 0

onConnect = { outgoing, incoming ->
	if( incoming == inputTerminal ){
		redraw()
	}
}

onDisconnect = { outgoing, incoming ->
	if( incoming == inputTerminal ){
		if( !options().contains( valueName.value ) ) valueName.value = 'Select value'
		redraw()
	}
}

onSignature = { outgoing, signature ->
	if( !options().contains( valueName.value ) ) valueName.value = 'Select value'
	redraw()
}

analyze = { message ->
	try{
		long timestamp = System.currentTimeMillis()
		
		if( !message.containsKey( valueName.value ) ) {
			if( failOnMissingValue.value ) {
				raiseFailure( message, timestamp, null )
				totalCounter.increment()
			}
			return
		}
		
		double val = message[valueName.value]
		
		if( sampleId.value ?: "" != "" && message[SampleCategory.SAMPLE_ID] != sampleId.value ) {
			if( failOnMissingID.value ) {
				raiseFailure( message, timestamp, val )
				totalCounter.increment()
			}
			return
		}
		
		if( val < min.value || val > max.value ) {
			synchronized( buffer ) {
				buffer.addLast( timestamp )
				if( buffer.size() >= tolerance.value ) {
					def lit = buffer.listIterator()
					//Remove old entries
					if( period.value > 0 ) {
						long oldest = timestamp - period.value * 1000
						while( lit.hasNext() ) if( lit.next() < oldest ) lit.remove()
					}
				}
				
				if( buffer.size() >= tolerance.value ) {
					raiseFailure(message, timestamp, val)
					buffer.clear()
				}
			}
		}
		
		totalCounter.increment()
	}
	catch(GroovyCastException e){
		log.debug("The Assertion component expects integers in Input Data, but it got something else.")
	}
	catch(Exception e){
		ex(e, "Assertion -> analyze")
	}
}

raiseFailure = {message, timestamp, value ->
	assertionFailureCounter.increment()
	failureCounter.increment()
	
	m = includeAssertedMessage.value ? message : outMsg
	
	m["Assert"] = valueName.value
	m["Min"] = min.value
	m["Value"] = value
	m["Max"] = max.value
	m["Period"] = period.value
	m["Tolerance"] = tolerance.value
	m["Timestamp"] = timestamp
	send(output, m)
}

onRelease = { 
	ReleasableUtils.releaseAll( counterStatisticSupport )
}

resetComponent = {
	buffer.clear()
	assertedResetValue = 0
	failedResetValue = 0
}

onAction( "RESET" ) { ->
	resetComponent()
}

ex = {t, m ->
	println("-------------------------------")
	println("exception in ${m} method in groovy")
	println("type: ${t}")
	println("message: ${t.getMessage()}")
	println("stacktrace:")
	int tCnt = 0
	for(item in t.getStackTrace()){
		if(tCnt == 0){
			println("${item}")
		}
		else{
			println("\t${item}")
		}
		tCnt++
	}
	println("-------------------------------")
}

def options() {
	inputTerminal.connections.collect( { it.outputTerminal.messageSignature.keySet() } ).flatten().toSet()
}

def redraw() {
	provider = new OptionsProviderImpl([])
	provider.options = options()
		
	//Layout
	layout {
		property( property: valueName, widget: 'comboBox', label: 'Value', options: provider, constraints: 'w 100!' )
		separator( vertical: true )
	//	box {
			property( property: min, label: 'Min', min: 0 )
			property( property: max, label: 'Max', min: 0 )
	//	}
		separator( vertical: true )
	//	box {
	//		property( property: tolerance, label: 'Tolerance', min: 1, constraints:'wrap 1')
	//		property( property: period, label: 'Period', min: 0 )
	//	}
	//	separator( vertical: true )
		box(layout:'wrap, ins 0') {
			box( widget:'display', layout:'wrap 2') {
				node( label:'Asserted', content: { totalCounter.get()-assertedResetValue }, constraints:'w 50!' )
				node( label:'Failed', content: { assertionFailureCounter.get()-failedResetValue }, constraints:'w 50!' )
				node( label:'Min', content: { min.value }, constraints:'w 50!' )
				node( label:'Max', content: { max.value }, constraints:'w 50!' )
			}
			action( 
				label: 'Reset', 
				action: {
					buffer.clear()
					assertedResetValue = totalCounter.get()
					failedResetValue = assertionFailureCounter.get()
				}, 
				constraints:'align right'
			)
		}
	}
}

redraw()

//Compact Layout
compactLayout {
	box( widget:'display' ) {
		node( label:'Value', content: { valueName.value }, constraints:'w 60!' )
		node( label:'Min', content: { min.value }, constraints:'w 60!' )
		node( label:'Max', content: { max.value }, constraints:'w 60!' )
		node( label:'Asserted', content: { totalCounter.get()-assertedResetValue } )
		node( label:'Failed', content: { assertionFailureCounter.get()-failedResetValue } )
	}
}

settings( label: "General" ) {
	property( property: tolerance, label: 'Tolerance', min: 1 )
	property( property: period, label: 'Period' )
	property( property: failOnMissingValue, label: 'Fail on missing value' )
	property( property: sampleId, label: 'Sample ID' )
	property( property: failOnMissingID, label: 'Fail on mismatching ID' )
	property( property: includeAssertedMessage, label: 'Include original message in failure messages' )
}