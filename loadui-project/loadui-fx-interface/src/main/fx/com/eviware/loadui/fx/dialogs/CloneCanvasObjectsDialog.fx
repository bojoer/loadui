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

package com.eviware.loadui.fx.dialogs;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;

import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.InputTerminal;

import com.eviware.loadui.fx.widgets.canvas.Canvas;
import com.eviware.loadui.fx.widgets.canvas.TestCaseNode;

import javafx.util.Sequences;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.lang.Duration;
import javafx.async.Task;

import java.util.HashMap;

public class CloneCanvasObjectsDialog {
	
	public-init var target:CanvasItem;
	
	public-init var objects:CanvasObjectItem[];
	
	postinit {
		
		var timeline: Timeline = Timeline {
	        repeatCount: 1
	        keyFrames: [
		        KeyFrame {
		            time: 500ms
		            action: function() {
						updateAndGo();
		            }
		            canSkip: true
		        }
	        ]
	    }
		
		var moveField:CheckBoxField;
		def dialog:Dialog = Dialog {
			title: "Clone objects"
			content: Form {
				margin: 8
				formContent: [
					LabelField { value: if( sizeof objects == 1 ) "Clone this item?" else "Clone these { sizeof objects } items?" },
					LabelField { value: "Target canvas: {target.getLabel()}" },
					moveField = CheckBoxField { label: "Move instead? (removes the objects from the current canvas)" },
				]
			}
			onOk: function() {
				moveComponents( moveField.value as Boolean );
				def canvasItem = objects[0].getCanvas();
				if(canvasItem instanceof SceneItem){
					//wait until current testcase is updated 
					//so the result miniature is correct
					timeline.playFromStart();	
				}
				else if(canvasItem instanceof ProjectItem){
					AppState.byName("MAIN").setActiveCanvas(target);
				}
				dialog.close();
			}
		}
	}
	
	function moveComponents( deleteInitial:Boolean ):CanvasObjectItem[] {
		def clones = new HashMap();
		def canvasItem = objects[0].getCanvas();
		for( object in objects ) clones.put( object, target.duplicate( object ) );
		for( connection in canvasItem.getConnections() ) {
			def inputComponent = connection.getInputTerminal().getTerminalHolder() as CanvasObjectItem;
			def outputComponent = connection.getOutputTerminal().getTerminalHolder() as CanvasObjectItem;
			if( Sequences.indexOf( objects, inputComponent ) >= 0 and Sequences.indexOf( objects, outputComponent ) >= 0 ) {
				def outputTerminal = (clones.get( outputComponent ) as CanvasObjectItem).getTerminalByName( connection.getOutputTerminal().getName() ) as OutputTerminal;
				def inputTerminal = (clones.get( inputComponent ) as CanvasObjectItem).getTerminalByName( connection.getInputTerminal().getName() ) as InputTerminal;
				target.connect( outputTerminal, inputTerminal );
			}
		}
		if( deleteInitial )
			for( object in objects ) object.delete();
		
		for( clone in clones.values() ) clone as CanvasObjectItem;
	}
	
	function updateAndGo(){
		MainWindow.instance.testcaseCanvas.refreshComponents();
		MainWindow.instance.testcaseCanvas.generateMiniatures();
		var tcn: TestCaseNode = MainWindow.instance.projectCanvas.lookupCanvasNode(objects[0].getCanvas().getId()) as TestCaseNode;
		tcn.loadMiniature();
		AppState.byName("MAIN").setActiveCanvas( target );
	}
}