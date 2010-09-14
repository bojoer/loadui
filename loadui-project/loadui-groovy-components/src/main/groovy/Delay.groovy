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
 * Delays incoming messages for a period of time
 * 
 * @help http://www.loadui.org/Flow-Control/delay-component.html
 * @category flow
 * @nonBlocking true
 */
 
import com.eviware.loadui.api.events.PropertyEvent
 
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.util.layout.DelayedFormattedString

final NONE = 'None'
final GAUSSIAN = 'Gaussian'
final UNIFORM = 'Uniform'

random = new Random()
waitingCount = 0

display = new DelayedFormattedString( ' %d /ms ', 500, 0 )
waitingDisplay = new DelayedFormattedString( ' %d  ', 500, value { waitingCount } )
 
output = createOutput( 'output', "Message Output" )
 
createProperty('delay', Long, 0)
createProperty('selected', String, NONE)
createProperty('randomDelay', Integer, 0)

executor = Executors.newSingleThreadScheduledExecutor()
 
onMessage = { incoming, outgoing, message ->
	waitingCount++
	
	long delayTime = delay.value 
	if( selected.value == GAUSSIAN ) {
		delayTime += (random.nextGaussian() * (randomDelay.value / 100) * delayTime * 0.3)
	} else if( selected.value == UNIFORM ) {
		delayTime += 2*(random.nextDouble() - 0.5 ) * delayTime * (randomDelay.value / 100)
	}
	
	message.put( 'actualDelay', delayTime )
	log.info "Delaying: $delayTime ms"
	executor.schedule( {
		send( output, message )
		waitingCount--
		display.args = delayTime
	}, delayTime, TimeUnit.MILLISECONDS )
 }
 
onRelease = {
	display.release()
	waitingDisplay.release()
	executor.shutdownNow()
}

addEventListener( ActionEvent ) { event ->
	if ( event.key == "STOP" && executor != null ) {
		executor.shutdownNow()
		executor = null
	}
	
	if ( event.key == "START" && executor == null ) {
		executor = Executors.newSingleThreadScheduledExecutor()
	}
	
	if ( event.key == "RESET" ) {
		display.args = 0
		waitingCount = 0;
		if( executor != null ) {
			executor.shutdownNow()
			executor = Executors.newSingleThreadScheduledExecutor()
		}
	}
}

layout { 
	property( property:delay, label:"Delay(ms)", min:0, step:100, span:60000 ) 
	separator( vertical:true )
	node(widget: 'selectorWidget', labal: 'Distribution', labels:[ NONE, GAUSSIAN, UNIFORM ], default: selected.value, selected: selected)
	property( property: randomDelay, label:'Random(%)', min:0, max: 100 )
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'delay ', fString:display, constraints:'w 60!' )
		node( label:'waiting ', fString:waitingDisplay, constraints:'w 50!' )
	}
}
 
compactLayout {
	box( widget:'display' ) {
		node( label:'delay ', fString:display, constraints:'w 60!' )
		node( label:'waiting ', fString:waitingDisplay, constraints:'w 50!' )
	}
}