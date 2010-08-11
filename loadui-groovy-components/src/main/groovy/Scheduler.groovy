import java.util.Calendar;

import java.util.Calendar;
import java.util.Date;

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
 * @help http://www.loadui.org/Schedulers/interval.html
 * @category scheduler
 * @nonBlocking true
 * @dependency org.quartz-scheduler:quartz:1.8.3
 * 
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
import org.quartz.Scheduler
import org.quartz.CronTrigger
import org.quartz.impl.StdSchedulerFactory
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.Job
import org.quartz.JobDetail
import java.util.Calendar
import org.quartz.listeners.JobListenerSupport

createProperty( 'day', String, "Every day" )
createProperty( 'hour', Long, -1 )
createProperty( 'minute', Long, -1 )
createProperty( 'second', Long, 0 )
def duration = createProperty( 'duration', Long, -1 )
def unit = createProperty( 'unit', String, 'Sec' )
def repeatCount = createProperty( 'repeatCount', Long, -1 )

def canvas = getCanvas()

def startMessage = newMessage()
startMessage[TriggerCategory.ENABLED_MESSAGE_PARAM] = true
sendStart = { send( outputTerminal, startMessage ) }

def stopMessage = newMessage()
stopMessage[TriggerCategory.ENABLED_MESSAGE_PARAM] = false
sendStop = { send( outputTerminal, stopMessage ) }

def counter = 0
def durationHolder = 0
def repeatHolder = 0

def scheduler = new StdSchedulerFactory().getScheduler()
scheduler.addJobListener(new JobListenerSupport()
{
	String getName(){
		"startJobListener"
	}
	void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		sendStart()
		scheduleEndTrigger()
		counter++
		if(repeatHolder > -1 && counter >= repeatHolder){
			unscheduleStartTrigger()
		}
	}
})
scheduler.addJobListener(new JobListenerSupport()
{
	String getName(){
		"endJobListener"
	}
	void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		sendStop()
		unscheduleEndTrigger()
	}
})

def paused = false

class SchedulerJob implements Job {
	void execute(JobExecutionContext context) throws JobExecutionException {}
}

def startTrigger = null
def startJob = new JobDetail("startJob", "group", SchedulerJob.class)
startJob.addJobListener("startJobListener")

def endTrigger = null
def endJob = new JobDetail("endJob", "group", SchedulerJob.class)
endJob.addJobListener("endJobListener")

addEventListener( ActionEvent ) { event ->
	if( event.key == CanvasItem.START_ACTION) {
		if(!paused){ 
			scheduleStartTrigger()
		}
		scheduler?.start()
		paused = false
	}
	else if( event.key == CanvasItem.STOP_ACTION) {
		scheduler?.standby()
		paused = true
	}
	else if( event.key == CanvasItem.COMPLETE_ACTION) {
		counter = 0
		paused = false
		unscheduleStartTrigger()
		unscheduleEndTrigger()
	}
	else if(event.key == CounterHolder.COUNTER_RESET_ACTION){
		paused = false
		unscheduleStartTrigger()
		unscheduleEndTrigger()
		scheduleStartTrigger()
		scheduler?.start()
		counter = 0
	}
}

//addEventListener( PropertyEvent ) { event ->
//	if( event.property in [ day, hour, minute, second, duration, unit, repeatCount ] ) {
		//if( !canvas.running ) scheduleStartTrigger()
//	}
//}

scheduleStartTrigger = {
	def startTriggerPattern = ""
	if(second.value == -1){
		startTriggerPattern += "* "
	}
	else{
		startTriggerPattern += "${second.value} "
	}
	if(minute.value == -1){
		startTriggerPattern += "* "
	}
	else{
		startTriggerPattern += "${minute.value} "
	}
	if(hour.value == -1){
		startTriggerPattern += "* "
	}
	else{
		startTriggerPattern += "${hour.value} "
	}
	startTriggerPattern += "? * "
	if(day.value.equals("Every day")){
		startTriggerPattern += "* "
	}
	else{
		startTriggerPattern += "${day.value.substring(0,3).toUpperCase()} "
	}
	
	unscheduleStartTrigger()
	scheduler.addJob(startJob, true)
	startTrigger = new CronTrigger("startTrigger", "group", "startJob", "group", startTriggerPattern)
	scheduler.scheduleJob(startTrigger)
	
	repeatHolder = repeatCount.value
	durationHolder = duration.value
}

unscheduleStartTrigger = {
	try{
		scheduler.unscheduleJob("startTrigger", "group")
	}
	catch(Exception e){}
}

scheduleEndTrigger = {
	if(durationHolder > -1){
		def calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		if(unit.value.equals("Sec")){
			calendar.add(Calendar.SECOND, (int)durationHolder)
		}
		else if(unit.value.equals("Min")){
			calendar.add(Calendar.MINUTE, (int)durationHolder)
		}
		else if(unit.value.equals("Hour")){
			calendar.add(Calendar.HOUR, (int)durationHolder)
		}
		def endTriggerPattern = ""
		endTriggerPattern += "${calendar.get(Calendar.SECOND)} "
		endTriggerPattern += "${calendar.get(Calendar.MINUTE)} "
		endTriggerPattern += "${calendar.get(Calendar.HOUR_OF_DAY)} "
		endTriggerPattern += "${calendar.get(Calendar.DAY_OF_MONTH)} "
		endTriggerPattern += "${calendar.get(Calendar.MONTH) + 1} "
		endTriggerPattern += "? "
		endTriggerPattern += "${calendar.get(Calendar.YEAR)} "
		
		unscheduleEndTrigger()
		scheduler.addJob(endJob, true)
		endTrigger = new CronTrigger("endTrigger", "group", "endJob", "group", endTriggerPattern)
		scheduler.scheduleJob(endTrigger)
	}
}

unscheduleEndTrigger = {
	try{
		scheduler.unscheduleJob("endTrigger", "group")
	}
	catch(Exception e){}
}

onRelease = {
	cancelAll()
}

cancelAll = {
	scheduler.shutdown()
}

layout() {
    property( property: day, widget: 'comboBox', label: 'Day', options: ['Every day', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'], constraints: 'w 100!' )
    separator( vertical: true )
    property( property: hour, label: 'Hour', min: -1, max: 23)
    property( property: minute, label: 'Min', min: -1, max: 59 )
	property( property: second, label: 'Sec', min: -1, max: 59 )
    separator( vertical: true )
    property( property: duration, label: 'Duration', min: -1 )
	property( property:unit, label: 'Unit', options: ['Sec','Min','Hour'] )
	separator( vertical: true )
    property( property: repeatCount, label: 'Repeat', min: -1)
}
