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
 * Schedule the start and stop of a trigger component.
 * On Start starts timer and when StartAt reached send one START message to 
 * attached component. When Duration expires one STOP message is send to each 
 * attached component.
 * Repeat option repeats whole process if counter limit not set.
 * 
 * On Stop it sends STOP message to attached components and stops timer.
 *
 * On Reset it just reset timer and stops it.
 *
 * @help http://www.loadui.org/Schedulers/interval-component.html
 * @category scheduler
 * @nonBlocking true
 */

import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.api.counter.CounterHolder

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import com.eviware.loadui.api.component.categories.TriggerCategory
import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.util.layout.IntervalModel
import com.eviware.loadui.util.ScheduledExecutor

createProperty( 'startAt', Long, 0 )
createProperty( 'duration', Long, 0 )
createProperty( 'unit', String, 'Sec' )
createProperty( 'mode', String, 'Single' )

def timerCounter = getCounter( CanvasItem.TIMER_COUNTER )
def canvas = getCanvas()
def runCount = 0
def running = canvas.running;

def startFuture = null
def startMessage = newMessage()
startMessage[TriggerCategory.ENABLED_MESSAGE_PARAM] = true
sendStart = { send( outputTerminal, startMessage ) }

def stopFuture = null
def stopMessage = newMessage()
stopMessage[TriggerCategory.ENABLED_MESSAGE_PARAM] = false
sendStop = { send( outputTerminal, stopMessage ) }

def endFuture = null

def executor = ScheduledExecutor.instance
def interval = new IntervalModel()

updateState = {
	long limit = canvas.getLimit( CanvasItem.TIMER_COUNTER ) * 1000
	
	long mult = 1000
	if( unit.value == 'Min' ) mult *= 60
	else if( unit.value == 'Percent' && limit > 0 ) mult = limit / 100
	
	long startTime = startAt.value * mult
	if( limit >= 0 && startTime > limit ) {
		startAt.value = limit / mult
		return
	}
	interval.start = startTime
	
	long stopTime = startTime + duration.value * mult
	if( limit >= 0 && stopTime > limit ) {
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
			stopFuture = executor.schedule( sendStop, stopTime - currentTime, TimeUnit.MILLISECONDS )
		}
		if( currentTime < interval.end ) {
			endFuture?.cancel( true )
			endFuture = executor.schedule( {
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
			startFuture = executor.schedule( sendStart, startTime - currentTime, TimeUnit.MILLISECONDS )
			sendStop()
		} else if( currentTime < stopTime ) {
			sendStart()
		}
	} else {
		sendStop()
	}
	
	interval.running = running
	interval.notifyObservers()
}

addEventListener( ActionEvent ) { event ->
	if( event.key == CanvasItem.START_ACTION ) {
		running = true
	} else if( event.key == CanvasItem.STOP_ACTION ) {
		running = false
	} else if( event.key == CounterHolder.COUNTER_RESET_ACTION ) {
		running = canvas.running
		interval.position = 0
		runCount = 0
	} else {
		return
	}
	
	cancelAll()
	updateState()
}

addEventListener( PropertyEvent ) { event ->
	if( event.property in [ startAt, duration, unit, mode ] ) {
		if( !canvas.running ) updateState()
	}
}

onRelease = {
	cancelAll()
}

cancelAll = {
	startFuture?.cancel( true )
	startFuture = null
	stopFuture?.cancel( true )
	stopFuture = null
	endFuture?.cancel( true )
	endFuture = null
}

layout() {
    node( widget:'intervalWidget', model:interval, constraints:'span 6' )
    separator( vertical: false )
    property( property: startAt, label:'Start At', min:0 )
    property( property: duration, label: 'Duration', min:0 )
    separator( vertical:true )
    property( property:unit, label:'Unit', options:['Sec','Min','Percent'] )
    separator( vertical:true )
    property( property:mode, label:'Mode', options:['Single','Repeat'])
}

updateState()