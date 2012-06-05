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
 * Runs a Groovy Script
 *
 * @name Script Runner
 * @category runners
 * @id com.eviware.ScriptRunner
 * @help http://loadui.org/Custom-Components/script-runner.html
 */

import com.eviware.loadui.impl.component.categories.RunnerBase.SampleCancelledException

import java.util.HashSet
import java.util.Collections

import groovy.lang.GroovyShell
import groovy.lang.Binding

//Properties
createProperty( 'scriptFile', File, null, false )
createProperty( 'cacheScriptContent', Boolean, true, false )
createProperty( 'setBinding', Boolean, true )
scriptContent = createProperty( '_scriptContent', String ) {
	parseScript()
}

runningSamples = Collections.synchronizedSet( new HashSet() )
shell = new GroovyShell()
script = null

requestResetValue = 0
sampleResetValue = 0
discardResetValue = 0
failedResetValue = 0
runButton = null

parseScript = {
	if( !scriptContent.value )
	{
		runButton?.enabled = false
		return
	}

	try {
		script = shell.parse( scriptContent.value )
		runButton?.enabled = true
	} catch( e ) {
		log.error( e.message, e )
		runButton?.enabled = false
	}
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
	try {
		runningSamples.add( Thread.currentThread() )
		if( controller && !cacheScriptContent.value )
			updateScript()
		if( setBinding.value )
			script.binding = new Binding( new HashMap( message ) )
		script.binding.setProperty( 'log', log )
		def result = script.run()
		message['Status'] = true
		if( result instanceof Map ) {
			 message.putAll( result )
		} else {
			 message['Result'] = String.valueOf( result )
		}
	} catch( Throwable e ) {
		if( e instanceof InterruptedException )
			 throw new SampleCancelledException()
		message['Status'] = false
		message['Result'] = e.toString()
		//failedRequestCounter.increment()
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
		return threads.length
	}
}

onRelease = {
	shell.resetLoadedClasses()
}

onAction( "RESET" ) { 
	if( controller ) updateScript()
	requestResetValue = 0
	sampleResetValue = 0
	discardResetValue = 0
	failedResetValue = 0
}

//Layout
layout {
	box( layout: 'wrap 2, ins 0' ) {
		property( property: scriptFile, label: 'Groovy Script File', constraints: 'w 300!, spanx 2', style: '-fx-font-size: 17pt' )
		property( property: cacheScriptContent, label: 'Cache script content', constraints: 'growx, spanx 2' )
		separator()
		runButton = action( label: 'Run Once', action: { triggerAction('SAMPLE') }, enabled: ( scriptFile.value && scriptFile.value.exists() ) )
		action( label: 'Abort Running Scripts', action: { triggerAction('CANCEL') } )
	}
	separator( vertical: true )
	box( layout: 'wrap, ins 0' ){
		box( widget: 'display', layout: 'wrap 3, align right', column: '[50|50|60]' ) {
			 node( label: 'Requests', content: { requestCounter.get() - requestResetValue } )
			 node( label: 'Running', content: { currentlyRunning } )
			 node( label: 'Completed', content: { sampleCounter.get() - sampleResetValue } )
			 node( label: 'Queued', content: { queueSize } )
			 node( label: 'Discarded', content: { discardCounter.get() - discardResetValue } )
			 node( label: 'Failed', content: { failureCounter.get() - failedResetValue } )
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
		node( label: 'Requests', content: { requestCounter.get() - requestResetValue } )
		node( label: 'Running', content: { currentlyRunning } )
		node( label: 'Completed', content: { sampleCounter.get() - sampleResetValue } )
		node( label: 'Queued', content: { queueSize } )
		node( label: 'Discarded', content: { discardCounter.get() - discardResetValue } )
		node( label: 'Failed', content: { failureCounter.get() - failedResetValue } )
	}
}

settings( label: "Basic" ) {
	property( property: setBinding, label: 'Make trigger message parameters available to the script' )
}