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

import com.eviware.loadui.api.model.WorkspaceItem
import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.api.events.EventHandler
import com.eviware.loadui.api.events.ActionEvent

def formatSeconds( total ) {
	if( total < 0 )
		return "--:--:--" 
	
	int seconds = total;
	int hours = seconds / 3600;
	seconds -= hours*3600;
	int minutes = seconds / 60;
	seconds -= minutes*60;
	
	String.format( "%02d:%02d:%02d", hours, minutes, seconds )
}

def displayLimit( limit ) {
	limit <= 0 ? "-" : limit
}

//Load the proper workspace
if( workspaceFile != null ) {
	workspace?.release()
	workspace = workspaceProvider.loadWorkspace( workspaceFile )
} else if( workspace == null ) {
	workspace = workspaceProvider.loadDefaultWorkspace()
}

def importRunners = workspace.getProperty( WorkspaceItem.IMPORT_MISSING_AGENTS_PROPERTY )
workspace.localMode = localMode

//If custom agents are provided, remove saved ones.
if( agents != null ) {
	for( runner in new ArrayList( workspace.runners ) )
		runner.delete()
	importRunners.value = false
} else {
	importRunners.value = true
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
def project = projectRef.getProject()

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

//Assign Runners
if( agents != null ) {
	for( agentUrl in agents.keySet() ) {
		def tcs = agents[agentUrl]
		def agent = workspace.createAgent( agentUrl, agentUrl )
		if( tcs == null )
		{
			for( tc in project.scenes ) {
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
				project.assignScene( tc, agent )
			}
		}
	}
}

//Define reports generation 
if( reports != null ) {
	def repDir = new File(reports);
	if(repDir.isFile()){
		log.error "Reports path points to a file instead of directory! Reports won't be saved!"
	}
	else{
		if(!repDir.isDirectory()){
			repDir.mkdirs();
		}
		project.setReportFolder(repDir.getAbsolutePath());
		project.setSaveReport(true);
	}
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

//Run the test
log.info """

------------------------------------
 TARGET ${target.label}
 LIMITS Time: ${formatSeconds(target.getLimit(CanvasItem.TIMER_COUNTER))} Samples: ${displayLimit(target.getLimit(CanvasItem.SAMPLE_COUNTER))} Failures: ${displayLimit(target.getLimit(CanvasItem.FAILURE_COUNTER))}
------------------------------------

"""

target.triggerAction( CanvasItem.START_ACTION )

def time = target.getCounter( CanvasItem.TIMER_COUNTER )
def samples = target.getCounter( CanvasItem.SAMPLE_COUNTER )
def failures = target.getCounter( CanvasItem.FAILURE_COUNTER )

//Monitor
while( target.summary == null ) {
	log.info "Time: ${formatSeconds(time.value)} Samples: ${samples.value} Failures: ${failures.value}"
	sleep 1000
}

//Shutdown
log.info """

------------------------------------
 TEST EXECUTION COMPLETED
 FINAL RESULTS: ${formatSeconds(time.value)} Samples: ${samples.value} Failures: ${failures.value}
------------------------------------

"""

log.info "Shutting down..."
sleep 1000

project.release()
workspace.release()