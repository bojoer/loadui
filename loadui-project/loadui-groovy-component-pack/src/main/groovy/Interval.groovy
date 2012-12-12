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
 * Schedules the start and stop of connected components 
 *
 * @id com.eviware.Interval
 * @help http://www.loadui.org/Schedulers/interval-component.html
 * @category scheduler
 * @nonBlocking true
 */
 
// Schedule the start and stop of a trigger component.
// On Start starts timer and when StartAt reached send one START message to 
// attached component. When Duration expires one STOP message is send to each 
// attached component.
// Repeat option repeats whole process if counter limit not set.
// 
// On Stop it sends a STOP message to attached components and stops timer.
//
// On Reset it just resets the timer and stops it.


import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.api.events.BaseEvent
import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.util.layout.IntervalModel

import java.util.concurrent.TimeUnit

createProperty( 'startAt', Long, 0 )
createProperty( 'duration', Long, 0 )
createProperty( 'unit', String, 'Sec' )
createProperty( 'mode', String, 'Single' )

def timerCounter = counters[CanvasItem.TIMER_COUNTER]
def canvas = getCanvas()
def runCount = 0

def startFuture = null
def stopFuture = null

def endFuture = null

def interval = new IntervalModel()

updateState = {
	long limit = canvas.getLimit( CanvasItem.TIMER_COUNTER ) * 1000
	
	long mult = 1000
	if( unit.value == 'Min' ) mult *= 60
	else if( unit.value == 'Percent' && limit > 0 ) mult = limit / 100
	
	long startTime = startAt.value * mult
	if( limit > 0 && startTime > limit ) {
		startAt.value = limit / mult
		return
	}
	interval.start = startTime
	
	long stopTime
	if(duration.value == 0){
		stopTime = (limit > 0) ? limit : IntervalModel.INFINITE
	}
	else{
		stopTime = startTime + duration.value * mult
	}
	
	if( limit > 0 && stopTime > limit ) {
		duration.value = limit / mult - startAt.value
		return
	}
	interval.stop = stopTime
	interval.end = ( limit > stopTime && mode.value == 'Single' ) ? limit : stopTime
	
	long currentTime = timerCounter.get() * 1000 - runCount * stopTime
	interval.position = currentTime
	
	if( running ) {
		if( currentTime < stopTime ) {
			stopFuture?.cancel( true )
			stopFuture = schedule( { sendEnabled( false ) }, stopTime - currentTime, TimeUnit.MILLISECONDS )
		}
		if( currentTime < interval.end ) {
			endFuture?.cancel( true )
			endFuture = schedule( {
				if( mode.value == 'Single' ) {
					intervalModel.stop()
					intervalModel.update()
				} else {
					runCount++
					interval.position = 0
					updateState()
				}
			}, interval.end - currentTime, TimeUnit.MILLISECONDS )
		}
		if( currentTime < startTime ) {
			startFuture?.cancel( true )
			startFuture = schedule( { sendEnabled( true ) }, startTime - currentTime, TimeUnit.MILLISECONDS )
			sendEnabled( false )
		} else if( currentTime < stopTime ) {
			sendEnabled( true )
		}
	} else {
		sendEnabled( false )
	}
	
	interval.running = running
	interval.notifyObservers()
}

onAction( "START" ) {
	cancelAll()
	updateState()
}

onAction( "STOP" ) {
	cancelAll()
	updateState()
}

onAction( "RESET" ) {
	interval.position = 0
	runCount = 0
}

addEventListener( PropertyEvent ) { event ->
	if( event.property in [ startAt, duration, unit, mode ] ) {
		if( !running ) updateState()
	}
}

def limitsListener = addEventListener( canvas, BaseEvent ) { event ->
	if( event.key == CanvasItem.LIMITS ) {
		if( !running ) updateState()
	}
}

onRelease = {
	cancelAll()
	canvas.removeEventListener(BaseEvent, limitsListener)
}

cancelAll = {
	cancelTasks()
	startFuture = null
	stopFuture = null
	endFuture = null
}

layout {
    node( widget:'intervalWidget', model:interval, constraints:'span 6, gaptop 10' )
    separator( vertical: false )
    property( property: startAt, label:'Start At', min:0 )
    property( property: duration, label: 'Duration', min:0 )
    separator( vertical:true )
    property( property:unit, label:'Unit', options:['Sec','Min','Percent'] )
    separator( vertical:true )
    property( property:mode, label:'Mode', options:['Single','Repeat'])
}

compactLayout {
	box( widget:'display' ) {
		node( label:'Start At', content: { "$startAt $unit" } )
		node( label:'Duration', content: { "$duration $unit" } )
	}
}

updateState()