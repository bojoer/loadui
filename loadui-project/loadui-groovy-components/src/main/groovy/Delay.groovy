// 
// Copyright 2011 eviware software ab
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
 
import java.util.concurrent.TimeUnit
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.util.layout.DelayedFormattedString
import com.eviware.loadui.api.model.WorkspaceItem
import com.eviware.loadui.api.model.SceneItem

final GAUSSIAN = 'Gaussian'
final UNIFORM = 'Uniform'
final EXPONENTIAL = 'Exponential'

random = new Random()
waitingCount = 0

display = new DelayedFormattedString( '%d ms', 500, 0 )
waitingDisplay = new DelayedFormattedString( '%d', 500, value { waitingCount } )
 
output = createOutput( 'output', "Message Output" )
 
createProperty('delay', Long, 0)
createProperty('selected', String, UNIFORM)
createProperty('randomDelay', Integer, 0)


workspace = canvas.project?.workspace
fixDisplay = {
	if( canvas instanceof SceneItem && !workspace?.localMode ) {
		display.format = 'n/a'
		waitingDisplay.format = 'n/a'
	} else {
		display.format = '%d ms'
		waitingDisplay.format = '%d'
	}
} 

def workspaceListener = null
if( workspace != null ) {
	workspaceListener = addEventListener( workspace, PropertyEvent ) { event ->
		fixDisplay()
	}
}
fixDisplay()
 
onMessage = { incoming, outgoing, message ->
	waitingCount++
	
	long delayTime = delay.value 
	if( selected.value == GAUSSIAN ) {
		delayTime += ( random.nextGaussian() * ( randomDelay.value / 100 ) * delayTime * 0.3)
	} else if( selected.value == UNIFORM ) {
		delayTime += 2*( random.nextDouble() - 0.5 ) * delayTime * ( randomDelay.value / 100 )
	} else if( selected.value == EXPONENTIAL ) {
		delayTime *= -Math.log( 1 - random.nextDouble() )
	}
	
	message.put( 'actualDelay', delayTime )
	schedule( {
		send( output, message )
		waitingCount--
		display.args = delayTime
	}, delayTime, TimeUnit.MILLISECONDS )
 }
 
onRelease = {
	display.release()
	waitingDisplay.release()
	workspace?.removeEventListener( PropertyEvent, workspaceListener )
}

onAction( "COMPLETE" ) {
	cancelTasks()
	waitingCount = 0
}

onAction( "RESET" ) {
	display.args = 0
	waitingCount = 0
	cancelTasks()
}

layout { 
	property( property:delay, label:"Delay(ms)", min:0, step:100, span:60000 ) 
	separator( vertical:true )
	node( widget:'selectorWidget', label:'Distribution', labels:[ UNIFORM, EXPONENTIAL, GAUSSIAN ],
		images:[ 'linear_shape.png', 'poisson_shape.png', 'gauss_shape.png' ], default: selected.value, selected: selected )
	property( property: randomDelay, label:'Random(%)', min:0, max: 100 )
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Delay ', fString:display, constraints:'w 60!' )
		node( label:'Waiting ', fString:waitingDisplay, constraints:'w 50!' )
	}
}
 
compactLayout {
	box( widget:'display' ) {
		node( label:'Delay ', fString:display, constraints:'w 60!' )
		node( label:'Waiting ', fString:waitingDisplay, constraints:'w 50!' )
	}
}