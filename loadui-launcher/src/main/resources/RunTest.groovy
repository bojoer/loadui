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

import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.api.events.EventHandler
import com.eviware.loadui.api.events.ActionEvent
import java.util.concurrent.Semaphore

//Load the proper workspace
if( workspaceFile != null ) {
	workspace?.release()
	workspace = workspaceProvider.loadWorkspace( workspaceFile )
} else if( workspace == null ) {
	workspace = workspaceProvider.loadDefaultWorkspace()
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
	log.error "TestCase '${testCase}' doesn't exist in Project '${project}'"
	return
}

//Set limits
if( limits != null ) {
	def names = [ CanvasItem.TIMER_COUNTER, CanvasItem.SAMPLE_COUNTER, CanvasItem.FAILURE_COUNTER ]
	for( limit in limits.split(";") ) {
		target.setLimit( names.remove( 0 ), Integer.parseInt( limit ) )
	}
}

//Run the test
log.info """
------------------------------------
TARGET ${target.label}
LIMITS Time: ${target.getLimit(CanvasItem.TIMER_COUNTER)} Samples: ${target.getLimit(CanvasItem.SAMPLE_COUNTER)} Failures: ${target.getLimit(CanvasItem.FAILURE_COUNTER)}
------------------------------------
"""
target.triggerAction( CanvasItem.START_ACTION )
def time = target.getCounter( CanvasItem.TIMER_COUNTER )
def samples = target.getCounter( CanvasItem.SAMPLE_COUNTER )
def failures = target.getCounter( CanvasItem.FAILURE_COUNTER )

//Monitor
while( target.summary == null ) {
	log.info "Time: ${time.value} Samples: ${samples.value} Failures: ${failures.value}"
	sleep 1000
}

//Shutdown
log.info """
------------------------------------
TEST EXECUTION COMPLETED
FINAL RESULTS: ${time.value} Samples: ${samples.value} Failures: ${failures.value}
------------------------------------
"""

log.info "Shutting down..."
sleep 1000

project.release()
workspace.release()