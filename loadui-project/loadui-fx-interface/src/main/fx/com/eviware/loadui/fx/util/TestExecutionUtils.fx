/* 
 * Copyright 2011 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */

package com.eviware.loadui.fx.util;

import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestState;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.util.BeanInjector;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.dialogs.Dialog;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.Runnable;

import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;

def WARN_STOPPING_TEST = "gui.warn_stopping_test";

def startingDialogTask = new StartingDialogTask();
def stoppingDialogTask = new StoppingDialogTask();

def testRunner = BeanInjector.getBean( TestRunner.class ) on replace {
	testRunner.registerTask( startingDialogTask, Phase.PRE_START, Phase.START );
	testRunner.registerTask( stoppingDialogTask, Phase.PRE_STOP );
}

public function startCanvas( canvas:CanvasItem ):TestExecution {
	var queuedExecutions:TestExecution[] = for( e in testRunner.getExecutionQueue() ) e;
	var currentExecution:TestExecution = null;
	
	if( sizeof queuedExecutions > 0 ) {
		currentExecution = queuedExecutions[0];
		queuedExecutions = queuedExecutions[1..];
	}
	
	if( currentExecution != null ) {
		if( not ( canvas instanceof ProjectItem ) and canvas.getProject() == currentExecution.getCanvas() ) {
			canvas.triggerAction( CanvasItem.START_ACTION );
			return null;
		}
		
		if( Boolean.parseBoolean(canvas.getProject().getAttribute( WARN_STOPPING_TEST, "true" ) ) ) {
			def checkbox = CheckBox { text: "Don't show this dialog again" }
			def dialog:Dialog = Dialog {
				title: "Stop current test?"
				content: [
					Label { text: "Starting {canvas.getLabel()} requires that the current test be stopped.\r\nDo you wish to stop the currently running test?\r\n" },
					checkbox
				]
				okText: "Yes"
				cancelText: "No"
				onOk: function() {
					if( checkbox.selected ) canvas.getProject().setAttribute( WARN_STOPPING_TEST, "false" );
					for( execution in queuedExecutions ) execution.abort();
					currentExecution.complete();
					testRunner.enqueueExecution( canvas );
					dialog.close();
				}
			}
			return null;
		} else {
			for( execution in queuedExecutions ) execution.abort();
			currentExecution.complete();
			return testRunner.enqueueExecution( canvas );
		}
	} else {
		testRunner.enqueueExecution( canvas );
	}
}

public function stopCanvas( canvas:CanvasItem ):TestExecution {
	def execution = currentExecution();
	if( execution != null ) {
		if( execution.getCanvas() == canvas ) {
			execution.complete();
			return execution;
		} else if( execution.getCanvas() == canvas.getProject() ) {
			canvas.triggerAction( CanvasItem.STOP_ACTION );
		}
	}
	
	return null;
}

public function currentExecution():TestExecution {
	def executions = for( e in testRunner.getExecutionQueue() ) e;
	return if( sizeof executions > 0 ) executions[0] else null;
}

public function abortAllExecutions():Void {
	for( execution in testRunner.getExecutionQueue() ) execution.abort();
}

public class TestExecutionUtils {
}

class StartingDialogTask extends TestExecutionTask {
	override function invoke( execution, phase ):Void {
		def canvas = execution.getCanvas();
		if( phase == Phase.PRE_START ) {
			FxUtils.runInFxThread( function():Void {
				def mainAppState = AppState.byName("MAIN");
				mainAppState.setBlockedText( "Initializing {canvas.getLabel()}." );
				mainAppState.setCancelHandler( function():Void {
					execution.abort();
				} );
				mainAppState.block();
			} );
		} else if( phase == Phase.START ) {
			FxUtils.runInFxThread( function():Void {
				AppState.byName("MAIN").unblock();
			} );
		}
	}
}

class StoppingDialogTask extends TestExecutionTask, Runnable {
	override function invoke( execution, phase ):Void {
		def canvas = execution.getCanvas();
		if( not canvas.isAbortOnFinish() ) {
			FxUtils.runInFxThread( function():Void {
				def mainAppState = AppState.byName("MAIN");
				mainAppState.setBlockedText( "Waiting for {canvas.getLabel()} to complete." );
				mainAppState.setCancelHandler( function() {
					// abort should cancel everything
					mainAppState.setBlockedText( "Aborting running requests..." );
					mainAppState.setCancelHandler( null );
					canvas.getProject().cancelScenes( false );
					canvas.getProject().cancelComponents();
				} );
				mainAppState.block();
				
				Futures.makeListenable( execution.complete() ).addListener( this, MoreExecutors.sameThreadExecutor() );
			} );
		}
	}
	
	override function run():Void {
		FxUtils.runInFxThread( function():Void {
			AppState.byName("MAIN").unblock();
		} );
	}
}