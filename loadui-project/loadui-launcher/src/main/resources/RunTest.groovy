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

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem
import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.api.events.EventHandler
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.util.FormattingUtils
import com.eviware.loadui.api.events.CollectionEvent
import com.eviware.loadui.api.messaging.MessageListener
import com.eviware.loadui.api.messaging.MessageEndpoint
import com.eviware.loadui.api.model.AgentItem

def agentMessageListener = new MessageListener() {
	
	def agents = new HashSet()
	
	public void put(AgentItem agent, SceneItem scene){
		agents.add("${agent.id}:${scene.id}")
		agent.addMessageListener(AgentItem.AGENT_CHANNEL, this)	
	}
	
	public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
	{
		Map<String, Object> map = ( Map<String, Object> )data;
		if( map.containsKey( AgentItem.STARTED ) )
		{
			logInfo "Test case ${map.get( AgentItem.STARTED )} on agent ${endpoint.url} initialized"
			agents.remove("${endpoint.id}:${map.get( AgentItem.STARTED )}")
		}
	}
	
	public boolean allTestCasesReady(){
		return agents.size() == 0
	}
}

def logInfo (GString m) {
	log.info m
}

def displayLimit( limit ) {
	limit <= 0 ? "-" : limit
}

log.info """

------------------------------------
 INITIALIZING COMMAND LINE RUNNER
------------------------------------

"""

//Load the proper workspace
if( workspaceFile != null ) {
	workspace?.release()
	log.info "Loading Workspace file: {}", workspaceFile.absolutePath
	workspace = workspaceProvider.loadWorkspace( workspaceFile )
} else if( workspace == null ) {
	log.info "Loading default Workspace"
	workspace = workspaceProvider.loadDefaultWorkspace()
}

def importAgents = workspace.getProperty( WorkspaceItem.IMPORT_MISSING_AGENTS_PROPERTY )
workspace.localMode = localMode

def projectAdded = false;
def workspaceCollectionListener = new EventHandler<CollectionEvent>() {
	public void handleEvent( CollectionEvent event ) {
		if( CollectionEvent.Event.ADDED == event.getEvent() && WorkspaceItem.PROJECTS.equals( event.getKey() ) )
		{
			projectAdded = true;
		}
	}
}
workspace.addEventListener( CollectionEvent.class, workspaceCollectionListener );

//If custom agents are provided, remove saved ones.
if( agents != null ) {
	for( agent in new ArrayList( workspace.agents ) )
		agent.delete()
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

def summaryExported = 0
def summaryExportListener = new EventHandler<BaseEvent>() {
	public void handleEvent( BaseEvent event ) {
		if( ProjectItem.SUMMARY_EXPORTED.equals( event.getKey() ) ) {
			summaryExported++;
		}
	}
}
project.addEventListener( BaseEvent.class, summaryExportListener )

//Get the target
def target = testCase ? project.getSceneByLabel( testCase ) : project
if( target == null ) {
	log.error "TestCase '${testCase}' doesn't exist in Project '${project.label}'"
	workspace?.release()
	return
}

//Set limits
if( limits != null ) {
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
if( agents != null ) {
	for( agentUrl in agents.keySet() ) {
		def tcs = agents[agentUrl]
		def agent = workspace.createAgent( agentUrl, agentUrl )
		if( tcs == null ) {
			for( tc in project.scenes ) {
				agentMessageListener.put(agent, tc)
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
				agentMessageListener.put(agent, tc)
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
if( testCase != null ) {
	def notReady = new HashSet()
	for( tc in project.scenes )
		for( agent in project.getAgentsAssignedTo( tc ) )
			notReady << agent
	def ready = false
	def timeout = System.currentTimeMillis() + 5000
	while( !ready ) {
		def stillNotReady = []
		for( agent in notReady )
			if( !agent.ready )
				stillNotReady << agent
		if( !stillNotReady.empty ) {
			notReady = stillNotReady
			if( System.currentTimeMillis() > timeout ) {
				log.error "Agents not connectable: ${notReady}"
				workspace?.release()
				return
			}
		} else {
			ready = true
		}
	}
	
	//TODO: Instead of waiting for 5 seconds here, it should be possible to find out from the agent that it is ready.
	sleep 5000
}

// wait until all test cases on all agents are ready
def waitForTestCases = !agentMessageListener.allTestCasesReady() 
if( waitForTestCases ){
	log.info "Start test case initialization..."
} 
def timeout = System.currentTimeMillis() + 60000
while( !agentMessageListener.allTestCasesReady() ){
	if(System.currentTimeMillis() >= timeout){
		log.error "Some test cases not initialized during timout period. Program will exit"
		workspace?.release()
		return
	}
	sleep 1000
}
if( waitForTestCases ){
	log.info "All test cases initialized properly"
}

// wait until workspace fires ADDED event for this
// project. this will ensure that RunningListener in
// ProjectManagerImpl is added to project before start
// so it can handle incoming events properly. Without
// this START event occurs before RunningLister is 
// assigned to the project and START event is not handled
// at all.  
while( !projectAdded ){
	sleep 1000
}

//Run the test
log.info """

------------------------------------
 RUNNING TEST
 TARGET ${target.label}
 LIMITS Time: ${FormattingUtils.formatTime(target.getLimit(CanvasItem.TIMER_COUNTER))} Samples: ${displayLimit(target.getLimit(CanvasItem.SAMPLE_COUNTER))} Failures: ${displayLimit(target.getLimit(CanvasItem.FAILURE_COUNTER))}
------------------------------------

"""

target.triggerAction( CanvasItem.START_ACTION )

def time = target.getCounter( CanvasItem.TIMER_COUNTER )
def samples = target.getCounter( CanvasItem.SAMPLE_COUNTER )
def failures = target.getCounter( CanvasItem.FAILURE_COUNTER )

//Monitor
while( target.summary == null ) {
	log.info "Time: ${FormattingUtils.formatTime(time.value)} Samples: ${samples.value} Failures: ${failures.value}"
	sleep 1000
}

//Wait for reports to be generated and saved (SUMMARY_EXPORTED is fired once when the summary is saved to the execution, then once again once the summary has been exported, but only if it should be exported).
while( summaryExported < ( project.saveReport ? 2 : 1 ) ) {
	sleep 1000
}

//Save Statistics report
if( statisticPages != null ) {
	//log.info "StatisticPages: $statisticPages"
}

//Shutdown
log.info """

------------------------------------
 TEST EXECUTION COMPLETED
 FINAL RESULTS: ${FormattingUtils.formatTime(time.value)} Samples: ${samples.value} Failures: ${failures.value}
------------------------------------

"""

def success = project.getLimit( CanvasItem.FAILURE_COUNTER ) == -1 || project.getCounter( CanvasItem.FAILURE_COUNTER ).get() < project.getLimit( CanvasItem.FAILURE_COUNTER )

log.info "Shutting down..."
sleep 1000

project.removeEventListener( BaseEvent.class, summaryExportListener )
project.release()

workspace.removeEventListener( CollectionEvent.class, workspaceCollectionListener )
workspace.release()

return success