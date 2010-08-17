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
import java.util.Date
import org.quartz.listeners.JobListenerSupport
import com.eviware.loadui.util.layout.DelayedFormattedString
import java.text.SimpleDateFormat
import com.eviware.loadui.impl.component.ActivityStrategies

createProperty( 'day', String, "Every day" )
createProperty( 'time', String, "0 * *" )
def duration = createProperty( 'duration', Long, 0 )
def runsCount = createProperty( 'runsCount', Long, 0 )

def canvas = getCanvas()

def startMessage = newMessage()
startMessage[TriggerCategory.ENABLED_MESSAGE_PARAM] = true
sendStart = { send( outputTerminal, startMessage ) }

def stopMessage = newMessage()
stopMessage[TriggerCategory.ENABLED_MESSAGE_PARAM] = false
sendStop = { send( outputTerminal, stopMessage ) }

class SchedulerJob implements Job {
	void execute(JobExecutionContext context) throws JobExecutionException {}
}

def startTrigger = null
def startJob = new JobDetail("startJob", "group", SchedulerJob.class)
startJob.addJobListener("startJobListener")

def endTrigger = null
def endJob = new JobDetail("endJob", "group", SchedulerJob.class)
endJob.addJobListener("endJobListener")

def counter = 0
def durationHolder = 0
def runsHolder = 0
def startSent = false

def paused = false
def pauseStart = -1
def pauseTotal = 0
def endTriggerStart = null //this is the time when latest enable event was sent
def rescheduleAfterPause = false
def endTriggerTimeLeft = null

def scheduler = new StdSchedulerFactory().getScheduler()
scheduler.addJobListener(new JobListenerSupport()
{
	String getName(){
		"startJobListener"
	}
	void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		sendStart()
		startSent = true
		endTriggerStart = new Date()
		scheduleEndTrigger(endTriggerStart, durationHolder)
		counter++
		if(runsHolder > 0 && counter >= runsHolder){
			unscheduleStartTrigger()
		}
		setActivityStrategy(ActivityStrategies.BLINKING)
		pauseTotal = 0
	}
})

scheduler.addJobListener(new JobListenerSupport()
{
	String getName(){
		"endJobListener"
	}
	void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		sendStop()
		setActivityStrategy(ActivityStrategies.OFF)
		unscheduleEndTrigger()
		endTrigger = null
		pauseTotal = 0
	}
})

addEventListener( ActionEvent ) { event ->
	if( event.key == CanvasItem.START_ACTION) {
		if(!paused){ 
			scheduleStartTrigger()
		}
		else if (rescheduleAfterPause){
			def now = new Date()
			pauseTotal += now.getTime() - pauseStart.getTime()
			scheduleEndTrigger(now, endTriggerStart.getTime() + durationHolder + pauseTotal - now.getTime())
			rescheduleAfterPause = false
		}
		scheduler?.start()
		paused = false
	}
	else if( event.key == CanvasItem.STOP_ACTION) {
		scheduler?.standby()
		paused = true
		pauseStart = new Date()
		if(endTrigger != null){
			unscheduleEndTrigger()
			endTrigger = null
			rescheduleAfterPause = true
		}
	}
	else if( event.key == CanvasItem.COMPLETE_ACTION) {
		reset()
	}
	else if(event.key == CounterHolder.COUNTER_RESET_ACTION){
		reset()
		scheduleStartTrigger()
		scheduler?.start()
	}
}

scheduleStartTrigger = {
	def startTriggerPattern = "${time.value} "
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
	
	runsHolder = runsCount.value
	durationHolder = duration.value * 1000
}

scheduleEndTrigger = {startTime, durationInMillis ->
	if(durationHolder > 0){
		def calendar = Calendar.getInstance()
		calendar.setTime(startTime)
		calendar.add(Calendar.MILLISECOND, (int)durationInMillis)

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

reset = {
	counter = 0
	durationHolder = 0
	runsHolder = 0
	paused = false
	pauseStart = -1
	pauseTotal = 0
	endTriggerStart = null
	rescheduleAfterPause = false
	endTriggerTimeLeft = null
	unscheduleStartTrigger()
	unscheduleEndTrigger()
	startTrigger = null
	endTrigger = null
	startSent = false
	setActivityStrategy(ActivityStrategies.OFF)
}

unscheduleStartTrigger = {
	try{
		scheduler.unscheduleJob("startTrigger", "group")
	}
	catch(Exception e){}
}

unscheduleEndTrigger = {
	try{
		scheduler.unscheduleJob("endTrigger", "group")
	}
	catch(Exception e){}
}

onRelease = {
	scheduler.shutdown()
	displayNextRun.release()
	displayTimeLeft.release()
}

displayNextRun = new DelayedFormattedString( '%s', 1000, value {
	if(startTrigger){
		def sdf = SimpleDateFormat.getInstance()
		sdf.setLenient(false)
		sdf.applyPattern("E HH:mm:ss")
		sdf.format(startTrigger.getFireTimeAfter(new Date()))
	}
	else{
		'Not running'
	}
})

displayTimeLeft = new DelayedFormattedString( '%s', 480, value {
	if(startSent && durationHolder == 0){
		'Infinite'
	}
	else if(endTrigger){
		def current = new Date()
		def next = endTrigger.getFireTimeAfter(current)
		def diff = 0
		if(next != null){
			diff = next.getTime() - current.getTime()
		}
		if(diff > 0){
			def sdf = SimpleDateFormat.getInstance()
			sdf.setLenient(false)
			if(diff < 3600000){
				sdf.applyPattern("00:mm:ss")
			}
			else{
				sdf.applyPattern("HH:mm:ss")
			}
			def calendar = Calendar.getInstance()
			calendar.setTimeInMillis(diff)
			endTriggerTimeLeft = sdf.format(calendar.getTime())
			endTriggerTimeLeft
		}
		else{
			'00:00:00'
		}
	}
	else{
		endTriggerTimeLeft ?: 'Not running'
	}
})

layout( constraints: 'gap 10 0') {
	box{
		property(property: day, widget: 'comboBox', label: 'Day', options: ['Every day', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'], constraints: 'w 100!, wrap' )
		property( property: runsCount, label: 'Runs', min: 0, constraints: 'align right')
	}
	separator(vertical: true)
	box{
		property( property: time, widget: 'quartzCronInput', label: 'Time', constraints: 'w 100!, wrap' )
		property( property: duration, widget: 'timeInput', label: 'Duration', constraints: 'w 100!' )
	}
	separator(vertical: true)
	box( widget:'display', constraints:'wrap' ) {
		node( label: 'Next Run', fString: displayNextRun, constraints: 'w 120!' )
		node( label: 'Time Left', fString: displayTimeLeft, constraints: 'w 120!' )
	}
}