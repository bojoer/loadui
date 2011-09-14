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
 * @id com.eviware.Delay
 * @help http://www.loadui.org/Flow-Control/delay-component.html
 * @category flow
 * @nonBlocking true
 */
 
import com.eviware.loadui.api.events.PropertyEvent
 
import java.util.concurrent.TimeUnit
import com.eviware.loadui.api.model.SceneItem

final GAUSSIAN = 'Gaussian'
final UNIFORM = 'Uniform'
final EXPONENTIAL = 'Exponential'

random = new Random()
waitingCount = 0

displayNA = false
displayCount = 0

createOutgoing( 'output' )
output.label = 'Delayed messages'
output.description = 'After being delayed, messages are outputted here.'

incomingTerminal.label = 'Messages to delay'
incomingTerminal.description = 'Recieved messages will be delayed before being outputted. Messages are processed independently in parallel (as opposed to being queued).'

createProperty('delay', Long, 0)
createProperty('selected', String, UNIFORM)
createProperty('randomDelay', Integer, 0)


workspace = canvas.project?.workspace
fixDisplay = { displayNA = canvas instanceof SceneItem && !workspace?.localMode }

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
		displayCount = delayTime
	}, delayTime, TimeUnit.MILLISECONDS )
 }
 
onRelease = {
	workspace?.removeEventListener( PropertyEvent, workspaceListener )
}

onAction( "COMPLETE" ) {
	cancelTasks()
	waitingCount = 0
}

onAction( "RESET" ) {
	displayCount = 0
	waitingCount = 0
	cancelTasks()
}

layout { 
	property( property:delay, label:"Delay\n(ms)", min:0, step:100, span:60000 ) 
	separator( vertical:true )
	node( widget:'selectorWidget', label:'Distribution', labels:[ UNIFORM, EXPONENTIAL, GAUSSIAN ],
		images:[ 'linear_shape.png', 'poisson_shape.png', 'gauss_shape.png' ], default: selected.value, selected: selected )
	property( property: randomDelay, label:'Random\n(%)', min:0, max: 100 )
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Delay ', content: { displayNA ? 'n/a' : "$displayCount ms" }, constraints:'w 60!' )
		node( label:'Waiting ', content: { displayNA ? 'n/a' : waitingCount }, constraints:'w 50!' )
	}
}
 
compactLayout {
	box( widget:'display' ) {
		node( label:'Delay ', content: { displayNA ? 'n/a' : "$displayCount ms" }, constraints:'w 60!' )
		node( label:'Waiting ', content: { displayNA ? 'n/a' : waitingCount }, constraints:'w 50!' )
	}
}