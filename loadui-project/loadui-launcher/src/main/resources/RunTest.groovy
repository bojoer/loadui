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

import com.eviware.loadui.api.events.BaseEvent
import com.eviware.loadui.api.model.ProjectItem
import com.eviware.loadui.api.model.SceneItem
import com.eviware.loadui.api.model.WorkspaceItem
import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.api.events.EventHandler
import com.eviware.loadui.api.events.CollectionEvent
import com.eviware.loadui.api.execution.TestRunner
import com.eviware.loadui.api.execution.TestState
import com.eviware.loadui.api.messaging.MessageListener
import com.eviware.loadui.api.messaging.MessageEndpoint
import com.eviware.loadui.api.model.AgentItem
import com.eviware.loadui.api.statistics.store.ExecutionManager
import com.eviware.loadui.api.statistics.ProjectExecutionManager
import com.eviware.loadui.api.reporting.ReportingManager
import com.eviware.loadui.util.BeanInjector
import com.eviware.loadui.util.FormattingUtils
import com.eviware.loadui.ui.fx.views.analysis.reporting.LineChartUtils
import com.google.common.io.Files

def log = log //Needed for agentMessageListener to be able to reference log.

def agentMessageListener = new MessageListener() {
	def agents = [] as Set
	
	public void putAt( AgentItem agent, SceneItem scene ) {
		agents.add("${agent.id}:${scene.id}")
		agent.addMessageListener( AgentItem.AGENT_CHANNEL, this )
	}
	
	public void handleMessage( String channel, MessageEndpoint endpoint, Object data ) {
		if( data.containsKey( AgentItem.STARTED ) ) {
			log.info "Test case ${data[AgentItem.STARTED]} on agent ${endpoint.url} initialized"
			agents.remove( "${endpoint.id}:${data[AgentItem.STARTED]}" )
		}
	}
	
	public boolean isTestCasesReady() {
		return agents.size() == 0
	}
}

def displayLimit( limit ) {
	limit <= 0 ? "-" : limit
}

log.info """

------------------------------------
 INITIALIZING COMMAND LINE RUNNER
------------------------------------

"""

//Load the proper workspace, use a copy of the file so that not changes are saved.
if( !workspace ) {
	def tmpWorkspace = File.createTempFile( "workspace", "xml" )
	def sourceFile = workspaceFile ?: workspaceProvider.defaultWorkspaceFile
	if( sourceFile.exists() ) {
		log.info "Loading Workspace file: {}", sourceFile.absolutePath
		Files.copy( sourceFile, tmpWorkspace )
	}
	workspace = workspaceProvider.loadWorkspace( tmpWorkspace )
}

def importAgents = workspace.getProperty( WorkspaceItem.IMPORT_MISSING_AGENTS_PROPERTY )
workspace.localMode = localMode

def projectAdded = false
def workspaceCollectionListener = new EventHandler<CollectionEvent>() {
	public void handleEvent( CollectionEvent event ) {
		if( CollectionEvent.Event.ADDED == event.event && WorkspaceItem.PROJECTS == event.key )
			projectAdded = true
	}
}
workspace.addEventListener( CollectionEvent, workspaceCollectionListener )

//If custom agents are provided, or in local mode, remove saved agents.
if( agents || workspace.localMode ) {
	log.info "Removing existing agents"
	for( agent in new ArrayList( workspace.agents ) ) {
		log.info "Removing: $agent, ${System.identityHashCode(agent)}"
		agent.delete()
	}
	importAgents.value = false
} else {
	importAgents.value = true
}

//Get the project. Import it if needed.
def projectRef = null
for( ref in workspace.projectRefs ) {
	if( ref.projectFile.absolutePath == projectFile.absolutePath ) {
		projectRef = ref
		break
	}
}
if( projectRef == null ) projectRef = workspace.importProject( projectFile, true )
log.info "Loading Project: {}", projectFile.absolutePath
projectRef.enabled = true
def project = projectRef.project

//def summaryExported = 0
//def summaryExportListener = new EventHandler<BaseEvent>() {
//	public void handleEvent( BaseEvent event ) {
//		if( ProjectItem.SUMMARY_EXPORTED == event.key ) {
//			summaryExported++
//		}
//	}
//}
//project.addEventListener( BaseEvent, summaryExportListener )

//Get the target
def target = testCase ? project.getSceneByLabel( testCase ) : project
if( !target ) {
	log.error "TestCase '${testCase}' doesn't exist in Project '${project.label}'"
	workspace?.release()
	return
}

//Set limits
if( limits ) {
	def names = [ CanvasItem.TIMER_COUNTER, CanvasItem.SAMPLE_COUNTER, CanvasItem.FAILURE_COUNTER ]
	for( limit in limits ) {
		try {
			target.setLimit( names.remove( 0 ), Integer.parseInt( limit ) )
		} catch( e ) {
			log.error( "Error setting limits:", e )
			workspace?.release()
			return
		}
	}
}

//Abort ongoing requests
if( abort?.toLowerCase()?.startsWith( "t" ) ) {
	project.abortOnFinish = true
} else if( abort?.toLowerCase()?.startsWith( "f" ) ) {
	project.abortOnFinish = false
}

//Assign Agents
if( agents ) {
	for( agentUrl in agents.keySet() ) {
		def tcs = agents[agentUrl]
		def agent = workspace.createAgent( agentUrl, agentUrl )
		if( tcs == null ) {
			for( tc in project.scenes ) {
				agentMessageListener[agent] = tc
				project.assignScene( tc, agent )
			}
		} else {
			for( tcLabel in tcs ) {
				def tc = project.getSceneByLabel( tcLabel )
				if( tc == null ) {
					log.error "TestCase '${tcLabel}' doesn't exist in Project '${project.label}'"
					workspace?.release()
					return
				}
				agentMessageListener[agent] = tc
				project.assignScene( tc, agent )
			}
		}
	}
}

//Define where report should be generated  
if( reportFolder != null ) {
	project.saveReport = true
	project.reportFormat = reportFormat
	def repFolder = new File( reportFolder )
	if( repFolder.file ) {
		project.reportFolder = repFolder.parentFile.absolutePath 
	} else {
		if( !repFolder.directory ) {
			repFolder.mkdirs()
		}
		project.reportFolder = repFolder.absolutePath
	}
	
	log.info "Saving '{}' reports to: {}", project.reportFormat, project.reportFolder
}

//Make sure all agents are ready
if( !workspace.localMode ) {
	log.info "Connectiong to agents..."
	def notReady = project.scenes.collect( { project.getAgentsAssignedTo( it ) } ).flatten() as Set
	def timeout = System.currentTimeMillis() + 20000
	while( !notReady.empty ) {
		if( System.currentTimeMillis() > timeout ) {
			log.error "Agents not connectable: ${notReady}"
			workspace?.release()
			return false
		}
		sleep 500
		notReady.removeAll { it.ready }
	}
}

// wait until all test cases on all agents are ready
if( !agentMessageListener.testCasesReady ) { 
	log.info "Awaiting remote TestCase initialization..."
	def timeout = System.currentTimeMillis() + 60000
	while( !agentMessageListener.testCasesReady ) {
		if( System.currentTimeMillis() >= timeout ) {
			log.error "Some TestCases not initialized during timeout period. Program will exit"
			workspace?.release()
			return
		}
		sleep 500
	}
	log.info "All TestCases initialized properly"
}

// wait until workspace fires ADDED event for this
// project. this will ensure that RunningListener in
// ProjectManagerImpl is added to project before start
// so it can handle incoming events properly. Without
// this START event occurs before RunningLister is 
// assigned to the project and START event is not handled
// at all.  
while( !projectAdded ) {
	sleep 100
}

//Run the test
log.info """

------------------------------------
 RUNNING TEST
 TARGET ${target.label}
 LIMITS Time: ${FormattingUtils.formatTime(target.getLimit(CanvasItem.TIMER_COUNTER))} Requests: ${displayLimit(target.getLimit(CanvasItem.SAMPLE_COUNTER))} Failures: ${displayLimit(target.getLimit(CanvasItem.FAILURE_COUNTER))}
------------------------------------

"""

for( a in workspace.agents ) {
	log.info "Agent: $a, ${System.identityHashCode(a)}"
}

def testRunner = BeanInjector.getBean( TestRunner )
def testExecution = testRunner.enqueueExecution( target )

def time = target.getCounter( CanvasItem.TIMER_COUNTER )
def samples = target.getCounter( CanvasItem.SAMPLE_COUNTER )
def failures = target.getCounter( CanvasItem.FAILURE_COUNTER )

//Monitor
while( testExecution.state != TestState.COMPLETED ) {
	log.info "Time: ${FormattingUtils.formatTime(time.value)} Requests: ${samples.value} Failures: ${failures.value}"
	sleep 1000
}

//Wait for reports to be generated and saved (SUMMARY_EXPORTED is fired once when the summary is saved to the execution, then once again once the summary has been exported, but only if it should be exported).
//while( summaryExported < ( project.saveReport ? 2 : 1 ) ) {
//	sleep 1000
//	log.info 'Waiting for summary generation to complete...'
//}

//Save Statistics report
if( statisticPages != null && project.reportFolder ) {
	def pages = statisticPages.empty ? project.statisticPages.children : project.statisticPages.children.findAll { statisticPages.contains( it.title ) }
	if( !retainZoom ) {
		for( page in pages ) {
			for( chartGroup in page.children ) {
				for( chartView in [ chartGroup.chartView, chartGroup.chartViewsForCharts, chartGroup.chartViewsForSources ].flatten() ) {
					chartView.setAttribute( "zoomLevel", "ALL" )
				}
			}
		}
	} else {
		log.info "Retaining Project zoom levels for charts"
	}
	def executionManager = BeanInjector.getBean( ExecutionManager )
	def execution = executionManager.currentExecution
	def comparedExecution = null
	if( compare ) {
		comparedExecution = BeanInjector.getBean( ProjectExecutionManager ).getExecutions( project ).find { it.label == compare }
		if( comparedExecution ) {
			log.info "Comparing to previous execution: $comparedExecution.label"
		} else {
			log.info "No execution with label '$compare' found!"
		}
	}
	def map = LineChartUtils.createImages( pages, execution, comparedExecution )
	def file = new File( project.reportFolder, FormattingUtils.formatFileName( "${project.label}-statistics-${execution.label}.${project.reportFormat.toLowerCase()}}" ) )
	if( includeSummary ) {
		BeanInjector.getBean( ReportingManager ).createReport( project.label, execution, pages, map, file, project.reportFormat, execution.summaryReport )
	} else {
		BeanInjector.getBean( ReportingManager ).createReport( project.label, execution, pages, map, file, project.reportFormat )
	}
}

//Shutdown
log.info """

------------------------------------
 TEST EXECUTION COMPLETED
 FINAL RESULTS: ${FormattingUtils.formatTime(time.value)} Requests: ${samples.value} Failures: ${failures.value}
------------------------------------

"""

def success = project.getLimit( CanvasItem.FAILURE_COUNTER ) == -1 || project.getCounter( CanvasItem.FAILURE_COUNTER ).get() < project.getLimit( CanvasItem.FAILURE_COUNTER )

log.info "Shutting down..."
sleep 1000

project.removeEventListener( BaseEvent, summaryExportListener )
project.release()

workspace.removeEventListener( CollectionEvent, workspaceCollectionListener )
workspace.release()

return success