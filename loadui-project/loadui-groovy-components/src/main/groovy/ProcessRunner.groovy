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
 * Runs a Operating System Process
 *
 * @name Process Runner
 * @category runners
 * @id com.eviware.ProcessRunner
 * @help http://loadui.org/Custom-Components/process-runner.html
 */

import com.eviware.loadui.util.layout.DelayedFormattedString
import com.eviware.loadui.util.ReleasableUtils
import com.eviware.loadui.impl.component.categories.RunnerBase.SampleCancelledException

import java.util.HashSet
import java.util.Collections
import com.eviware.loadui.impl.component.ActivityStrategies

import java.util.concurrent.TimeUnit

import groovy.lang.GroovyShell
import groovy.lang.Binding

//Properties
createProperty( 'command', String, "")
createProperty( 'scriptFile', File )
createProperty( 'cacheScriptContent', Boolean, true )
createProperty( 'setBinding', Boolean, true )
scriptContent = createProperty( '_scriptContent', String ) {
	parseScript()
}

updateLed = {
	setActivityStrategy( currentlyRunning > 0 ? ActivityStrategies.BLINKING : ActivityStrategies.ON )
}

future = scheduleAtFixedRate( { updateLed() }, 500, 500, TimeUnit.MILLISECONDS )

runningSamples = Collections.synchronizedSet( new HashSet() )
shell = new GroovyShell()
script = null

requestResetValue = 0
sampleResetValue = 0
discardResetValue = 0
failedResetValue = 0
runButton = null

displayRequests = new DelayedFormattedString( '%d', 500, value { requestCounter.get() - requestResetValue } )
displayRunning = new DelayedFormattedString( '%d', 500, value {
currentlyRunning } )
displayTotal = new DelayedFormattedString( '%d', 500,  value {
sampleCounter.get() - sampleResetValue } )
displayQueue = new DelayedFormattedString( '%d', 500, value { queueSize } )
displayDiscarded = new DelayedFormattedString( '%d', 500,  value {
discardCounter.get() - discardResetValue } )
displayFailed = new DelayedFormattedString( '%d', 500,  value {
failureCounter.get() - failedResetValue } )

parseScript = {
	if( command.value != "" )
		runButton?.enabled = false
}

//We'll only ever read the file on the controller, and send out the script content as a String to the agents.
if( controller ) {
	lastModified = null
	updateScript = {
		if( scriptFile.value && scriptFile.value.exists() ) {
			if( lastModified != scriptFile.value.lastModified() ) {
				scriptContent.value = scriptFile.value.text
				lastModified = scriptFile.value.lastModified()
				parseScript()
			}
		} else {
			lastModified = null
			scriptContent.value = null
		}
	}
	updateScript()
	
	onReplace( scriptFile, updateScript )
	onReplace( cacheScriptContent, updateScript )
}


sample = { message, sampleId ->
	try
	{
		runningSamples.add( Thread.currentThread() )
	
		// start a process and wait for it to finish
		def proc = command.value.execute()
		proc.waitFor()                              

		// add result properties
		message["ExitValue"] = proc.exitValue()
		message["Stdout"] = proc.in.text
		message["Errout"] = proc.err.text

		// fail if process failed
		if( proc.exitValue() != 0 )
		   failureCounter.increment()
	}
	catch( e )
	{
		// add error properties
		message["ExitValue"] = -1
		message["Errout"] = e.message

		failureCounter.increment()
	} finally {
		runningSamples.remove( Thread.currentThread() )
	}

	return message
}

onCancel = {
	synchronized( runningSamples ) {
		def threads = runningSamples.toArray()
		runningSamples.clear()
		threads.each { it.interrupt() }
	}
}

onRelease = {
	ReleasableUtils.releaseAll( displayRunning, displayTotal, displayQueue, displayDiscarded, displayFailed, displayRequests)
	shell.resetLoadedClasses()
}

onAction( "RESET" ) { 
	requestResetValue = 0
	sampleResetValue = 0
	discardResetValue = 0
	failedResetValue = 0
}

//Layout
layout {
	box( layout: 'wrap 2, ins 0' ) {
		property( property:command, label:"Command", constraints: 'growx, span 2' )
		separator()
		runButton = action( label: 'Run Once', action: { triggerAction('SAMPLE') }, enabled: ( scriptFile.value && scriptFile.value.exists() ) )
		action( label: 'Abort Running Commands', action: { triggerAction('CANCEL') } )
	}
	separator( vertical: true )
	box( layout: 'wrap, ins 0' ){
		box( widget: 'display', layout: 'wrap 3, align right', column: '[50|50|60]' ) {
			 node( label: 'Requests', fString: displayRequests )
			 node( label: 'Running', fString: displayRunning )
			 node( label: 'Completed', fString: displayTotal )
			 node( label: 'Queued', fString: displayQueue )
			 node( label: 'Discarded', fString: displayDiscarded )
			 node( label: 'Failed', fString: displayFailed )
		}
		action( label: 'Reset', action: {
			 requestResetValue = requestCounter.get()
			 sampleResetValue = sampleCounter.get()
			 discardResetValue = discardCounter.get()
			 failedResetValue = failureCounter.get()
			 triggerAction('CANCEL')
		}, constraints:'align right' )
	}
}

compactLayout {
	box( widget: 'display', layout: 'wrap 3, align right', column: '[50|50|60]' ) {
		node( label: 'Requests', fString: displayRequests )
		node( label: 'Running', fString: displayRunning )
		node( label: 'Completed', fString: displayTotal )
		node( label: 'Queued', fString: displayQueue )
		node( label: 'Discarded', fString: displayDiscarded )
		node( label: 'Failed', fString: displayFailed )
	}
}

settings( label: "Basic" ) {
	property( property: setBinding, label: 'Make trigger message parameters available to the script' )
}