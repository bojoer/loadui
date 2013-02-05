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
 * Schedules the start and stop at a specified day & time
 *
 * @id com.eviware.Scheduler
 * @help http://www.loadui.org/Schedulers/scheduler-component.html
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

import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.util.layout.IntervalModel
import com.eviware.loadui.util.ScheduledExecutor
import com.eviware.loadui.util.FormattingUtils
import com.eviware.loadui.util.ReleasableUtils
import org.quartz.Scheduler
import org.quartz.CronTrigger
import org.quartz.CronExpression
import org.quartz.impl.StdSchedulerFactory
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.Job
import org.quartz.JobDetail
import java.util.Calendar
import java.util.Date
import org.quartz.listeners.JobListenerSupport
import java.text.SimpleDateFormat
import com.eviware.loadui.util.layout.SchedulerModel

// Each component instace has its own group since they all share
// same scheduler instance. This group level should be unique for 
// each component instance.
def SCHEDULER_GROUP = this.toString()

// names of jobs, they can be the same in all instances because
// they are added to different groups
def START_JOB_NAME = "startJob"
def END_JOB_NAME = "endJob"

// by specifying unique name make sure that each component instance
// has its own listeners
def START_JOB_LISTENER_NAME = "${SCHEDULER_GROUP}startJobListener"
def END_JOB_LISTENER_NAME = "${SCHEDULER_GROUP}endJobListener"

// names of triggers, they can be the same in all instances because
// they are added to different groups
def START_TRIGGER_NAME = "startTrigger"
def END_TRIGGER_NAME = "endTrigger"

def counter = 0
def durationHolder = 0
def runsHolder = 0
def startSent = false

def schedulerModel = new SchedulerModel()

createProperty( 'day', String, "* (All)" )
createProperty( 'time', String, "0 0 0" )
def duration = createProperty( 'duration', Long, 0 )
def runsLimit = createProperty( 'runsLimit', Long, 0 )

def canvas = getCanvas()

sendStart = { 
	sendEnabled( true ) 
	startSent = true
	counter++
	if(runsHolder > 0 && counter >= runsHolder){
		unscheduleStartTrigger()
	}
	pauseTotal = 0
}

sendStop = { 
	sendEnabled( false ) 
	unscheduleEndTrigger()
	endTrigger = null
	pauseTotal = 0
}

class SchedulerJob implements Job {
	void execute(JobExecutionContext context) throws JobExecutionException {}
}

def startTrigger = null
def startJob = new JobDetail(START_JOB_NAME, SCHEDULER_GROUP, SchedulerJob.class)
startJob.addJobListener(START_JOB_LISTENER_NAME)

def endTrigger = null
def endJob = new JobDetail(END_JOB_NAME, SCHEDULER_GROUP, SchedulerJob.class)
endJob.addJobListener(END_JOB_LISTENER_NAME)

def paused = false
def pauseStart = -1
def pauseTotal = 0
def endTriggerStart = null //this is the time when latest enable event was sent
def rescheduleAfterPause = false
def endTriggerTimeLeft = null

def maxDuration = 0;

// create not initialized StdSchedulerFactory (no properties files supplied)
// this factory always returns the same instance of scheduler (shared between components)
def scheduler = new StdSchedulerFactory().getScheduler()

scheduler.addJobListener(new JobListenerSupport()
{
	String getName(){
		START_JOB_LISTENER_NAME
	}
	void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		sendStart()
		scheduleEndTrigger(new Date(), durationHolder)
	}
})

scheduler.addJobListener(new JobListenerSupport()
{
	String getName(){
		END_JOB_LISTENER_NAME
	}
	void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		sendStop()
		schedulerModel.incrementRunsCounter()
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

addEventListener( PropertyEvent ) { event ->
	if( event.property in [ day, time, runsLimit, duration ] ) {
		validateDuration()
		if( !canvas.running ){
			updateState()
		} 
	}
}

validateDuration = {
	def expr = new CronExpression(createStartTriggerPattern())
	def calendar = Calendar.getInstance()
	def nextDate = expr.getNextValidTimeAfter(calendar.getTime())
	calendar.setTime(nextDate)
	calendar.add(Calendar.SECOND, 1)
	def dateAfterNext = expr.getNextValidTimeAfter(calendar.getTime())
	def diff = dateAfterNext.getTime() - nextDate.getTime()
	if(diff/1000 < duration.value){
		duration.value = diff/1000
	}
	maxDuration = diff
}

updateState = {
	def expr = new CronExpression(createStartTriggerPattern())
	schedulerModel.setSeconds(expr.seconds)
	schedulerModel.setMinutes(expr.minutes)
	schedulerModel.setHours(expr.hours)
	schedulerModel.setDays(expr.daysOfWeek)
	schedulerModel.setDuration(duration.value * 1000)
	schedulerModel.setMaxDuration(maxDuration)
	schedulerModel.setRunsLimit((int)runsLimit.value)
	schedulerModel.notifyObservers()
}

createStartTriggerPattern = {
	def startTriggerPattern = "${time.value} "
	startTriggerPattern += "? * "
	if(day.value.equals("* (All)")){
		startTriggerPattern += "* "
	}
	else{
		startTriggerPattern += "${day.value.substring(0,3).toUpperCase()} "
	}
	startTriggerPattern
}

scheduleStartTrigger = {
	runsHolder = runsLimit.value
	durationHolder = duration.value * 1000
	
	def startTriggerPattern = createStartTriggerPattern()
	unscheduleStartTrigger()
	scheduler.addJob(startJob, true)
	startTrigger = new CronTrigger(START_TRIGGER_NAME, SCHEDULER_GROUP, START_JOB_NAME, SCHEDULER_GROUP, startTriggerPattern)
	scheduler.scheduleJob(startTrigger)
	
	def now = new Date()
	def next = startTrigger.getFireTimeAfter(now)
	if(now.getTime() <= next.getTime() - maxDuration + durationHolder){
		sendStart()
		scheduleEndTrigger(now, next.getTime() - maxDuration + durationHolder - now.getTime())
	}
	else{
		sendStop()
	}
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
		endTrigger = new CronTrigger(END_TRIGGER_NAME, SCHEDULER_GROUP, END_JOB_NAME, SCHEDULER_GROUP, endTriggerPattern)
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
	schedulerModel.resetRunsCounter()
}

unscheduleStartTrigger = {
	try{
		scheduler.unscheduleJob(START_TRIGGER_NAME, SCHEDULER_GROUP)
	}
	catch(Exception e){}
}

unscheduleEndTrigger = {
	try{
		scheduler.unscheduleJob(END_TRIGGER_NAME, SCHEDULER_GROUP)
	}
	catch(Exception e){}
}

onRelease = { scheduler.shutdown() }

layout {
	node( widget: 'schedulerWidget', model: schedulerModel, constraints: 'span 5, gaptop 10' )
	separator( vertical: false )
	property(property: day, widget: 'comboBox', label: 'Day', options: ['* (All)', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'], constraints: 'w 100!' )
	separator(vertical: true)
	property( property: time, widget: 'quartzCron', label: 'Time', constraints: 'w 130!' )
	separator(vertical: true)
	property( property: duration, widget: 'time', label: 'Duration', constraints: 'w 130!' )
}

compactLayout {
	box( widget:'display' ) {
		node( label:'Day', content: { day.value } )
		node( label:'Time', content: { ssmmhh = time.value.split(" ") -> "${ssmmhh[2]}:${ssmmhh[1]}:${ssmmhh[0]}" } )
		node( label:'Duration', content: { FormattingUtils.formatTime( duration.value ) } )
	}
}

settings( label: "Basic" ) {
	property( property: runsLimit, label: 'Runs')
}

validateDuration()
updateState()