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
*ProjectCanvas.fx
*
*Created on apr 29, 2010, 10:01:12 fm
*/

package com.eviware.loadui.fx.widgets.canvas;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.widgets.toolbar.ComponentToolbarItem;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.events.CollectionEvent;

import java.util.EventObject;
import java.lang.RuntimeException;

import com.eviware.loadui.fx.widgets.Trashcan;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.dialogs.ProjectSettingsDialog;
import com.eviware.loadui.fx.MainWindow;
import javafx.scene.text.Text;

import com.eviware.loadui.fx.widgets.toolbar.TestCaseToolbarItem;
import javafx.util.Math;
import java.io.File;
import javafx.scene.Group;
import javafx.scene.control.CheckBox;

import com.eviware.loadui.fx.util.ImageUtil.*;

public class ProjectCanvas extends Canvas {

	public-read var projectItem:ProjectItem;
	
	override var trashcan = Trashcan { layoutX: bind offsetX + scene.width - 120, layoutY: bind offsetY + 90 };
	
	override var canvasItem on replace oldCanvas {
		projectItem = canvasItem as ProjectItem; 
		if(canvasItem != null and not (canvasItem instanceof ProjectItem)) 
			throw new RuntimeException( "ProjectCanvas can only take a ProjectItem!" ) ;
		
		if (canvasItem != null) {
			for(testcase in projectItem.getChildren()) {
				addTestCase( testcase );
			}
			
			for( connection in projectItem.getConnections() ) {
				removeConnection( connection );
				addConnection( connection );
			}
			
			projectItem.getWorkspace().setAttribute( "lastOpenProject", projectItem.getProjectFile().getAbsolutePath() );
		}
	}
	
	override function generateMiniatures() {
		canvasItem.setAttribute("miniature", createMiniatures(155 - 18 - 4, 100 - 37 - 4, 0.1));
	    
	    var projectRef;
	    for(pRef in MainWindow.instance.workspace.getProjectRefs()){
			if(pRef.isEnabled() and pRef.getProject() == canvasItem){
				projectRef = pRef;
				break;
			}
		}
		if(projectRef != null){
			var base64: String = canvasItem.getAttribute("miniature", "");
		    projectRef.setAttribute("miniature", base64);
	    }
	}
	
	override function acceptFunction( d:Draggable ) {
		if (d instanceof TestCaseToolbarItem) {
			true;
		} else {
			super.acceptFunction( d );
		}
	}
	
	override function onDropFunction( d:Draggable ) {
		//TODO: If 'TestCaseToolbarItem', create a TestCase instead.
		if (d instanceof TestCaseToolbarItem) {
			def sb = d.node.localToScene(d.node.layoutBounds);
			def x = sb.minX;
			def y = sb.minY;
			log.debug( "TestCase dropped at: (\{\}, \{\})", x, y );
			def testcase = createTestCase();
			testcase.setAttribute( "gui.layoutX", "{offsetX + x as Integer}" );
			testcase.setAttribute( "gui.layoutY", "{offsetY + y as Integer}" );
		} else {
			super.onDropFunction( d );
		}
	}
	
	public function createTestCase():SceneItem {
		var name = "Virtual User Scenario";
		var i=0;
		while( sizeof projectItem.getChildren()[c|c.getLabel() == name] > 0 )
			name = "Virtual User Scenario ({++i})";
			
		if ( projectItem.getAttribute( ProjectSettingsDialog.IGNORE_UNASSIGNED_TESTCASES, "false" ) == "false" and not MainWindow.instance.workspace.isLocalMode()) {
			var checkbox:CheckBox;
		    def warning:Dialog = Dialog {
		    	title: "Warning!"
		    	content: [
		    		Text { content: "Switch to local mode, or place {name} on an agent in order to run it" },
		    		checkbox = CheckBox { selected: false, text: "Don't show this dialog again" }
		    	]
		    	okText: "Ok"
		    	onOk: function() {
		    		if( checkbox.selected ) projectItem.setAttribute( ProjectSettingsDialog.IGNORE_UNASSIGNED_TESTCASES, "true" );
		    		warning.close();
		    	}
		    	noCancel: true
		    }
		}

		log.debug( "Creating SceneItem using label: \{\}", name );
		
		projectItem.createScene( name );
		
	}

	function addTestCase( testCase:SceneItem ):Void {
		log.debug( "Adding SceneItem \{\}", testCase );
		def tstc = TestCaseNode.create( testCase, this );
		insert tstc into componentLayer.content;
	}

	override function handleEvent( e:EventObject ) {
		super.handleEvent( e );
		
		if( e instanceof CollectionEvent ) {
			def event = e as CollectionEvent;
			if( ProjectItem.SCENES.equals( event.getKey() ) ) {
				if( event.getEvent() == CollectionEvent.Event.ADDED ) {
					runInFxThread( function() { addTestCase( event.getElement() as SceneItem ) } );
				} else {
					runInFxThread( function() { removeModelItem( event.getElement() as SceneItem ); refreshComponents(); } );
				}
			}
		}
	}
}
