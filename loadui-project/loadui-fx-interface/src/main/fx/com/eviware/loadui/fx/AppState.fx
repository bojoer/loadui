/* 
 * Copyright 2010 eviware software ab
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

import com.eviware.loadui.api.ui.ApplicationState;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.async.BlockingTask;
import com.eviware.loadui.fx.dialogs.SaveProjectDialog;
import com.eviware.loadui.fx.ui.XWipePanel;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.widgets.canvas.TestCaseNode;
import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.Map;
import javafx.async.Task;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Panel;
import javafx.scene.layout.Container;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.jfxtras.animation.wipe.FadeWipe;
import org.jfxtras.animation.wipe.FadeZoomWipe;
import org.jfxtras.animation.wipe.Flip180Wipe;
import org.jfxtras.animation.wipe.Wipe;
import org.slf4j.LoggerFactory;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Math;

import com.eviware.loadui.fx.wizards.NewProjectWizard;

public def WORKSPACE_FRONT = "workspace.front";
public def PROJECT_FRONT = "project.front";
public def PROJECT_BACK = "project.back";
public def TESTCASE_FRONT = "testcase.front";

public def FADE_WIPE = FadeWipe { time: 250ms };
public def ZOOM_WIPE = FadeZoomWipe { time: 250ms };
public def FLIP_WIPE = Flip180Wipe { time: 250ms };
public def FLIP_WIPE2 = Flip180Wipe { time: 1500ms, direction: Flip180Wipe.LEFT_TO_RIGHT};

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.AppState" );

/**
 * The instance of the AppState singleton.
 */
public-read var instance:AppState;


def workaround = Timeline {
	keyFrames: KeyFrame { time: 250ms, action: function() {
		if( dummyNode.parent == null ) {
			insert dummyNode into instance.overlayLayer.content;
		} else {
			delete dummyNode from instance.overlayLayer.content;
		}
		dummyNode.layoutX = 100*Math.random();
	} }
	repeatCount: Timeline.INDEFINITE
};
def dummyNode = Rectangle { fill: Color.TRANSPARENT };
public var overlay:Node[] on replace {
	//delete dummyNode from instance.overlayLayer.content;
	instance.overlayLayer.content = overlay;
	//FX.deferAction( function():Void {
	//	if( dummyNode.parent == null )
	//		insert dummyNode into instance.overlayLayer.content;
	//} );
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
	def overlayLayer = Group { content: dummyNode };
	
	def localLayer = bind lazy wipePanel.content[0] as Group;
	var wipePanel:XWipePanel;
	def layers = Group { autoSizeChildren: false };
	
	def localNodes:Map = new HashMap();

	/**
	 * The scene to place the AppState into.
	 */
	public var scene:Scene on replace {
		log.debug( "Placing AppState layers into scene." );
		scene.content = layers;
	}
	
	def blocked = Rectangle {
		width: bind scene.width
		height: bind scene.height
		opacity: 0.3
		blocksMouse: true
		cursor: Cursor.WAIT
	}
	
	postinit {
		instance = this;
		for( s in [ WORKSPACE_FRONT, PROJECT_FRONT, PROJECT_BACK, TESTCASE_FRONT ] )
			localNodes.put( s, NodeSequenceWrapper { nodes: [] } );
		
		layers.content = [
			wipePanel = XWipePanel{ content: Group {}, width: bind scene.width, height: bind scene.height },
			globalLayer,
			overlayLayer
		];
		
		workaround.play();
	}
	
	/**
	 * Runs a task in the background, blocking user input until completed.
	 * The task function will not be run in the JavaFx thread, and must thus not modify the scenegraph.
	 * The onDone function will be called when the task completes, successful or not, with the Task object, which can be used to verify completion status. This will be called in the JavaFX thread.
	 */
	public function blockingTask( task:function():Void, onDone:function(task:Task):Void ):Void {
		insert blocked into overlay;
		def blockingTask:BlockingTask = BlockingTask {
			task:task
			onDone:function() {
				delete blocked from overlay;
				onDone( blockingTask );
			}
		};
		blockingTask.start();
	}
	
	override function getLoadedWorkspace():WorkspaceItem {
		MainWindow.instance.workspace;
	}
	
	override function setActiveCanvas( canvas:CanvasItem ) {
		FxUtils.runInFxThread(function():Void {
			if( canvas instanceof ProjectItem ) {
				MainWindow.instance.projectCanvas.canvasItem = canvas;
				transitionTo( AppState.PROJECT_FRONT, AppState.ZOOM_WIPE );
				if (canvas.isLoadingError()) {
				    def dialog:Dialog = Dialog {
				        noCancel: true
				        title: "Component Errors"
				        content: Text {
				            content: "Some of the components could not be loaded"
				        }
				        onOk: function() { dialog.close(); }
				    }
				}
				var project:ProjectItem = canvas as ProjectItem;
				
				if (project.getScenes().size() == 0 and project.getComponents().size() == 0 and project.getAttributes().size() == 0) {
					var newWizard:NewProjectWizard = NewProjectWizard{workspace: (canvas as ProjectItem).getWorkspace()};
					newWizard.show();
					
				}
			} else if( canvas instanceof SceneItem ) {
				MainWindow.instance.testcaseCanvas.canvasItem = canvas;
				transitionTo( AppState.TESTCASE_FRONT, AppState.ZOOM_WIPE );
			}
		});
	}
	
	override function getActiveCanvas():CanvasItem {
		if( state == PROJECT_FRONT ) {
			MainWindow.instance.projectCanvas.canvasItem;
		} else if( state == TESTCASE_FRONT ) {
			MainWindow.instance.testcaseCanvas.canvasItem;
		} else null;
	}
	
	override function displayWorkspace():Void {
		def active = getActiveCanvas();
		if( active != null ) {
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
								AppState.instance.transitionTo( AppState.WORKSPACE_FRONT, ZOOM_WIPE );
							} 
						}
				    } else {
						MainWindow.instance.testcaseCanvas.canvasItem = null;
						MainWindow.instance.projectCanvas.canvasItem = null;
						pRef.setEnabled( false );
						AppState.instance.transitionTo( AppState.WORKSPACE_FRONT, ZOOM_WIPE );
				    }
				}
			}
		}
	}
	
	/**
	 * Inserts a node into the localLayer for the given state.
	 * The state must be one of the predefined ones.
	 */
	public function insertInto( node:Node, state:String ):Void {
		def wrapper = localNodes.get( state ) as NodeSequenceWrapper;
		if( wrapper == null )
			throw new IllegalArgumentException( "State does not exist!" );
		
		insert node into wrapper.nodes;
		if( this.state == state )
			insert node into localLayer.content;
	}
	
	/**
	 * Removes the node from the localLayer for the given state.
	 * The state must be one of the predefined ones.
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
		
		if ( this.state == TESTCASE_FRONT ) {
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
