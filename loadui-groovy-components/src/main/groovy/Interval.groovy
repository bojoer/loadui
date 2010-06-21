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
 * 
 * On Stop it sends STOP message to attached components and stops timer.
 *
 * On Reset it just reset timer and stops it.
 *
 * @category scheduler
 * @nonBlocking true
 */

import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.api.model.CanvasItem

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import com.eviware.loadui.api.component.categories.TriggerCategory

import com.eviware.loadui.api.events.PropertyEvent

import com.eviware.loadui.impl.layout.IntervalObservableModel

createProperty('startAt', Long, 0)
createProperty('duration', Long, 0)
createProperty('unit', String, 'Sec')

def timerCounter = getCounter( CanvasItem.TIMER_COUNTER )
def canvas = getCanvas()

executor = Executors.newSingleThreadScheduledExecutor()

def task

def intervalModel = new IntervalObservableModel()

intervalModel.setStart(startAt.value)
intervalModel.setDuration(duration.value)
if ( canvas.getLimit(CanvasItem.TIMER_COUNTER) > -1 ) {
  intervalModel.setInterval(canvas.getLimit(CanvasItem.TIMER_COUNTER))
} else {
  intervalModel.setInterval(duration.value + startAt.value)
}

sendStart = {
	def message = newMessage()
	message[TriggerCategory.ENABLED_MESSAGE_PARAM] = true
	send( outgoingTerminal, message )
}

sendStop = {
	def message = newMessage()
	message[TriggerCategory.ENABLED_MESSAGE_PARAM] = false
	send( outgoingTerminal, message )
}

calculateTime = { time ->
    multiplier = 1000
    if ( unit.value == 'Min' )
	multiplier *= 60
    if ( unit.value == 'Percent' ) {
	limit = canvas.getLimit(CanvasItem.TIMER_COUNTER)
        if ( limit > 0 ) {
            // in this case time is percent of limit
            return (limit * (time/100) * 1000) as Long 
        } else {
	   println "[interval] limit not set. change unit!"
	   return -1
	}
    }
    time * multiplier
}

startTimer = { start, duration, current ->
	if ( start > current ) {
		if ( (unit.value == 'Percent').and(  (start + duration) >  100 ) ) {
			println "[interval] error setting percents (start + duration) > 100!"
			return
                }
		startTaskDelay = calculateTime(start) //seconds
		if ( startTaskDelay != -1 )
			executor.schedule( { 
				sendStart()
				task = executor.schedule ({sendStop()}, calculateTime(duration), TimeUnit.MILLISECONDS)
			}, startTaskDelay, TimeUnit.MILLISECONDS )
	} 
}

stopTimer = { 
	sendStop()
	task?.cancel(true)
	executor.shutdownNow()
	executor = Executors.newSingleThreadScheduledExecutor()
 }

addEventListener( ActionEvent ) { event ->

	if ( event.key == "STOP" ) {
		stopTimer()
		
		intervalModel.stop()
		setModelInterval()
		intervalModel.update()
	}
	
	if ( event.key == "START" ) {
	  if ( canvas.isRunning() ) {
		startTimer(startAt.value, duration.value, timerCounter.get())
		intervalModel.start()
		setModelInterval()
	  }
	}
	
	if ( event.key == "RESET" ) {
		task?.cancel(true)
		executor.shutdownNow()
		executor = Executors.newSingleThreadScheduledExecutor()
				
		setModelInterval()
	}
}

addEventListener( PropertyEvent ) { event ->
  if( event.event == PropertyEvent.Event.VALUE ) {

	if ( !canvas.isRunning() ) {
		setModelInterval()
	}
  }
}

setModelInterval = {
	multiplier = 1
	if ( unit.value == 'Percent' ) {
		if ( canvas.getLimit(CanvasItem.TIMER_COUNTER) > -1 ) {
		  if ( startAt.value > 100 ) {
			startAt.value = 100
                        duration.value = 0
                  } 
                  if ( startAt.value + duration.value > 100 ) 
			duration.value = 100 - startAt.value
		
		  intervalModel.setInterval(canvas.getLimit(CanvasItem.TIMER_COUNTER))
		  intervalModel.setStart( (canvas.getLimit(CanvasItem.TIMER_COUNTER)*startAt.value/100) as Long )
	          intervalModel.setDuration( (canvas.getLimit(CanvasItem.TIMER_COUNTER)*duration.value/100) as Long)
		  intervalModel.update()
		} else {
		   println("Percent is not working if limit is not set.")
		}
	} else {
	
	if( unit.value == 'Min' )
           multiplier = 60
      
	  startTime = startAt.value * multiplier
	  durationTime = duration.value * multiplier

          if ( canvas.getLimit(CanvasItem.TIMER_COUNTER) > -1 ) {
	      if ( startTime > canvas.getLimit(CanvasItem.TIMER_COUNTER) ) {
	  	startAt.value = 0
		startTime = 0
	      }
	      if ( durationTime + startTime > canvas.getLimit(CanvasItem.TIMER_COUNTER)) {
	  	durationTime = canvas.getLimit(CanvasItem.TIMER_COUNTER) - startTime;
	        duration.value = durationTime/multiplier
	      }
	      
              intervalModel.setInterval(canvas.getLimit(CanvasItem.TIMER_COUNTER))
	    
          } else {
	     intervalModel.setInterval(durationTime + startTime)
	  }
        
	  intervalModel.setStart(startTime)
	  intervalModel.setDuration(durationTime)
	}
	intervalModel.update()
}

onRelease = {

	task?.cancel(true)
	executor?.shutdownNow()
	
}

layout() {
    node( widget:'intervalWidget', model:intervalModel, constraints:'span 4' )
	separator( vertical: false )
	property( property: startAt, label:'Start At', min:0 )
	property( property: duration, label: 'Duration', min:0 )
	separator( vertical:true )
	property( property:unit, label:'Unit', options:['Sec','Min','Percent'] )
}
