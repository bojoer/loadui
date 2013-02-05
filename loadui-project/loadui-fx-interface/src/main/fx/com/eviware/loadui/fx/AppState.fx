/* 
 * Copyright 2011 SmartBear Software
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
/*
*AppState.fx
*
*Created on feb 24, 2010, 11:00:23 fm
*/

package com.eviware.loadui.fx;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.ui.ApplicationState;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.async.BlockingTask;
import com.eviware.loadui.fx.dialogs.SaveProjectDialog;
import com.eviware.loadui.fx.ui.XWipePanel;
import com.eviware.loadui.fx.ui.WaitingScreen;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.widgets.canvas.TestCaseNode;
import com.eviware.loadui.fx.widgets.canvas.Selectable;
import com.eviware.loadui.fx.util.TestExecutionUtils;
import java.lang.System;
import java.lang.IllegalArgumentException;
import java.lang.NullPointerException;
import java.util.HashMap;
import java.util.Map;
import javafx.async.Task;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Panel;
import javafx.scene.layout.Stack;
import javafx.scene.layout.Container;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.jfxtras.animation.wipe.FadeWipe;
import org.jfxtras.animation.wipe.FadeZoomWipe;
import org.jfxtras.animation.wipe.Flip180Wipe;
import org.jfxtras.animation.wipe.Wipe;
import org.slf4j.LoggerFactory;
import javafx.util.Math;
import javafx.util.Sequences;
import com.google.common.base.Strings;

import com.eviware.loadui.fx.wizards.NewProjectWizard;

public def FADE_WIPE = FadeWipe { time: 250ms };
public def ZOOM_WIPE = FadeZoomWipe { time: 250ms };
public def FLIP_WIPE = Flip180Wipe { time: 250ms };
public def FLIP_WIPE2 = Flip180Wipe { time: 1500ms, direction: Flip180Wipe.LEFT_TO_RIGHT};

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.AppState" );

def appStates = new HashMap();
def appStatesByName = new HashMap();

public-read var activeState:AppState; 

public function put( scene:Scene, appState:AppState, name:String ):Void {
	appStates.put( scene, appState );
	appStatesByName.put( name, appState );
	appState.scene = scene;
}

public function byScene( scene:Scene ):AppState {
	return appStates.get( scene ) as AppState;
}

public function byName( name:String ):AppState {
	return appStatesByName.get( name ) as AppState;
}

public var titleBarExtra:String on replace {
	for( appState in appStates.values() ) (appState as AppState).updateTitleBar();
}

/**
 * Keeps the state of the application and makes it available through the state var.
 * Allows access to the application layers globalLayer and overlayLayer directly, 
 * and indirect access to the localLayer through the insertInto and deleteFrom methods.
 * Allows changing the current state to one of the predefined ones. 
 *
 * @author dain.nilsson
 */
public class AppState extends ApplicationState {
	/**
	 * The current state of the application.
	 */
	public-read var state:String;
	
	/**
	 * A Group placed above the localLayer. Its content will always be displayed.
	 */
	public def globalLayer = Group {};
	
	/**
	 * A Group placed above the globalLayer.
	 * Should be used for Nodes which must be positioned on top of everything else,
	 * such as dialog boxes or popup menus.
	 */
	def overlayLayer = Group { id: "Overlay Layer", /*content: dummyNode*/ };
	public def overlay = Overlay { group: overlayLayer };
	
	def localLayer = bind lazy wipePanel.content[0] as Group;
	var wipePanel:XWipePanel;
	def layers = Group { autoSizeChildren: false };
	
	def localNodes:Map = new HashMap();
	def applicationName = System.getProperty( LoadUI.NAME );
	
	public var windowName:String on replace { updateTitleBar() }
	
	public function lookup( id:String ):Node {
		for( layer in [ localLayer, globalLayer ] ) {
			def node = layer.lookup( id );
			if( node != null ) {
				return node;
			}
		}
		
		return null;
	}

	/**
	 * The scene to place the AppState into.
	 */
	public var scene:Scene on replace {
		if( scene != null ) {
			log.debug( "Placing AppState layers into scene." );
			scene.content = layers;
			updateTitleBar()
		}
	}
	
	public function updateTitleBar() {
		scene.stage.title = "{applicationName} {LoadUI.VERSION}{if(not Strings.isNullOrEmpty(windowName)) ' - {windowName}' else ''}{if(not Strings.isNullOrEmpty(titleBarExtra)) ' ({titleBarExtra})' else ''}";
	}
	
	public-read def containsFocus = bind scene.stage.containsFocus on replace { if( containsFocus ) activeState = this }
	
	def blocked = WaitingScreen {
		width: bind scene.width
		height: bind scene.height
	}
	
	postinit {
		layers.content = [
			wipePanel = XWipePanel{ content: Group {}, width: bind scene.width, height: bind scene.height },
			globalLayer,
			overlayLayer
		];
	}
	
	/**
	 * Runs a task in the background, blocking user input until completed.
	 * The task function will not be run in the JavaFx thread, and must thus not modify the scenegraph.
	 * The onDone function will be called when the task completes, successful or not, with the Task object, which can be used to verify completion status. This will be called in the JavaFX thread.
	 */
	public function blockingTask( task:function():Void, onDone:function(task:Task):Void, text:String ):Void {
		blockingTask( task, onDone, text, null );
	}
	
	public function blockingTask( task:function():Void, onDone:function(task:Task):Void, text:String, onCancel:function():Void ):Void {
		setBlockedText( text );
		setCancelHandler( onCancel );
		block();
		def blockingTask:BlockingTask = BlockingTask {
			task:task
			onDone:function() {
				unblock();
				setBlockedText( "Waiting..." );
				onDone( blockingTask );
			}
		};
		blockingTask.start();
	}
	var blockCount = 0;
	public function block():Void { if( blockCount == 0 ) insert blocked into overlay.content; blockCount++; }
	
	public function unblock():Void { blockCount = Math.max( 0, blockCount-1 ); if( blockCount == 0 ) delete blocked from overlay.content; }
	
	public function setBlockedText( text:String ):Void { blocked.text = text; }
	
	public function setCancelHandler( onCancel:function():Void ):Void {
		blocked.onAbort = onCancel;
	}
	
	override function getLoadedWorkspace():WorkspaceItem {
		MainWindow.instance.workspace;
	}
	
	override function setActiveCanvas( canvas:CanvasItem ) {
		FxUtils.runInFxThread(function():Void {
			Selectable.selectNone();
			if( canvas instanceof ProjectItem ) {
				MainWindow.instance.projectCanvas.canvasItem = canvas;
				MainWindow.instance.projectCanvas.setNoteLayer( true );
				def lastStateWasTestCase = (MainWindow.TESTCASE_FRONT == state );
				transitionTo( MainWindow.PROJECT_FRONT, AppState.ZOOM_WIPE );
				if (canvas.isLoadingError() and not lastStateWasTestCase) {
				    def dialog:Dialog = Dialog {
				        noCancel: true
				        title: "Component Errors"
				        content: Text {
				            content: "Some of the components could not be loaded. Either the components are missing, or they contain errors. \n They will be removed from project."
				        }
				        onOk: function() { dialog.close(); }
				    }
				}
				var project:ProjectItem = canvas as ProjectItem;
				
				if (MainWindow.instance.workspace.getAttribute( NewProjectWizard.SHOW_PROJECT_WIZARD, "true" ) == "true" and project.getChildren().size() == 0 and project.getComponents().size() == 0 and project.getAttributes()[s|s.startsWith( "gui.note." )].size() == 0) {
					var newWizard:NewProjectWizard = NewProjectWizard{workspace: (canvas as ProjectItem).getWorkspace()};
					newWizard.show();
					
				}
			} else if( canvas instanceof SceneItem ) {
				MainWindow.instance.testcaseCanvas.canvasItem = canvas;
				transitionTo( MainWindow.TESTCASE_FRONT, AppState.ZOOM_WIPE );
				MainWindow.instance.testcaseCanvas.setNoteLayer( true );
			}
		});
	}
	
	override function getActiveCanvas():CanvasItem {
		if( state == MainWindow.PROJECT_FRONT ) {
			MainWindow.instance.projectCanvas.canvasItem;
		} else if( state == MainWindow.TESTCASE_FRONT ) {
			MainWindow.instance.testcaseCanvas.canvasItem;
		} else null;
	}
	
	override function displayWorkspace():Void {
		def active = getActiveCanvas();
		if( active != null ) {
			TestExecutionUtils.abortAllExecutions();
			def project = active.getProject();
			def workspace = project.getWorkspace();
			for( pRef in workspace.getProjectRefs() ) {
				if( pRef.isEnabled() and pRef.getProject() == project) {
				    if ( project.isDirty() ) {
						SaveProjectDialog {
							projectRef: pRef
							onDone: function():Void {
								MainWindow.instance.testcaseCanvas.canvasItem = null;
								MainWindow.instance.projectCanvas.canvasItem = null;
								pRef.setEnabled( false );
								AppState.byName("MAIN").transitionTo( MainWindow.WORKSPACE_FRONT, ZOOM_WIPE );
							} 
						}
				    } else {
						MainWindow.instance.testcaseCanvas.canvasItem = null;
						MainWindow.instance.projectCanvas.canvasItem = null;
						pRef.setEnabled( false );
						AppState.byName("MAIN").transitionTo( MainWindow.WORKSPACE_FRONT, ZOOM_WIPE );
				    }
				}
			}
		}
	}
	
	/**
	 * Inserts a node into the localLayer for the given state.
	 */
	public function insertInto( node:Node, state:String ):Void {
		if( not localNodes.containsKey( state ) ) {
			localNodes.put( state, NodeSequenceWrapper { nodes: [] } );
		}
		
		def wrapper = localNodes.get( state ) as NodeSequenceWrapper;
		if( wrapper == null )
			throw new IllegalArgumentException( "State does not exist!" );
		
		insert node into wrapper.nodes;
		if( this.state == state )
			insert node into localLayer.content;
	}
	
	/**
	 * Removes the node from the localLayer for the given state.
	 */
	public function deleteFrom( node:Node, state:String ):Void {
		def wrapper = localNodes.get( state ) as NodeSequenceWrapper;
		if( wrapper == null )
			throw new IllegalArgumentException( "State does not exist!" );
		
		delete node from wrapper.nodes;
		if( this.state == state )
			delete node from localLayer.content;
	}
	
	/**
	 * Clears all nodes from the localLayer for the given state.
	 */
	public function clearState( state:String ):Void {
		def wrapper = localNodes.get( state ) as NodeSequenceWrapper;
		if( wrapper == null )
			throw new IllegalArgumentException( "State does not exist!" );
		
		wrapper.nodes = null;
		if( this.state == state )
			localLayer.content = null;
	}
	
	/**
	 * Changes the current state of the application to the given state.
	 * Optionally uses a Wipe to animate this transition.
	 */
	public function transitionTo( state:String, wipe:Wipe ):Void {
		if( this.state == state )
			return;
		
		log.debug( "Transitioning from state \{\} to state \{\}.", this.state, state );
		
		if ( this.state == MainWindow.TESTCASE_FRONT ) {
		    var tc:SceneItem = MainWindow.instance.testcaseCanvas.canvasItem as SceneItem;
		    if( tc != null ) {
		    	var tcn:TestCaseNode = MainWindow.instance.projectCanvas.lookupCanvasNode( tc.getId() ) as TestCaseNode;
		    	tcn.loadMiniature();
		    }
		}
		
		def wrapper = localNodes.get( state ) as NodeSequenceWrapper;
		if( wrapper == null )
			throw new IllegalArgumentException( "State does not exist!" );
		
		this.state = state;
		def oldGroup = localLayer;
		
		def newGroup = Group {
			content: wrapper.nodes
		}
		
		if( wipe != null ) {
			log.debug( "Transitioning using wipe: \{\}.", wipe );
			wipePanel.wipe = wipe;
			wipePanel.action = function() { oldGroup.content = [] };
			//TODO: This causes a compiler error: wipePanel.next( newGroup );
			wipePanel.content = newGroup;
		} else {
			wipePanel.content = newGroup;
			oldGroup.content = null;
		}
	}
}

class NodeSequenceWrapper {
	public var nodes:Node[];
}
