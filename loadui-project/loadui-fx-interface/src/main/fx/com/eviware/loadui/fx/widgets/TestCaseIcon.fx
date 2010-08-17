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
*ProjectNode.fx
*
*Created on feb 10, 2010, 11:47:11 fm
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Line;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.resources.TitlebarPanel;
import com.eviware.loadui.api.model.SceneItem;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;

import java.util.EventObject;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.model.CanvasItem;

import java.util.EventObject;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;
import com.eviware.loadui.fx.AppState;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.TestCaseIcon" );

def ledActive = Image { url: "{__ROOT__}images/png/led-active.png" }
def ledInactive = Image { url: "{__ROOT__}images/png/led-inactive.png" }


/**
 * Node to display in the AgentList representing a AgentItem.
 */
public class TestCaseIcon extends BaseNode, Draggable, ModelItemHolder, EventHandler {
	/**
	 * The AgentItem to represent.
	 */
	public-init var agent: AgentItem;
	
	override var modelItem = bind lazy agent;
	
	public var sceneItem: SceneItem on replace oldScene {
		oldScene.removeEventListener(ActionEvent.class, this);
		sceneItem.addEventListener(ActionEvent.class, this);
	};
	
	public var isPlaceholder: Boolean = false;
	
	public-init var stateListeners: TestCaseIconListener[] = [];
	
	public function addTestCaseListener(stateListener: TestCaseIconListener){
		insert stateListener into stateListeners;
	}
	
	public function removeTestCaseListener(stateListener: TestCaseIconListener){
		delete stateListener from stateListeners;
	}
	
	function fireTestCaseIconSelected(){
		if(stateListeners != null){
			for(sl in stateListeners){
				sl.selectionChanged(this);
			}
		}
	}
	
	function fireTestCaseRemoved(){
		if(stateListeners != null){
			for(sl in stateListeners){
				sl.testCaseRemoved(this);
			}
		}
	}
	
	public-init var label: String = sceneItem.getLabel();
	
	public var selected: Boolean = false;
	
	public var width: Number = 95;
	
	public var height: Number = 20;
	
	var running: Boolean = sceneItem.isRunning();
	
	override function handleEvent(e: EventObject) {
		if( e instanceof ActionEvent ) {
			def event = e as ActionEvent;
			if(CanvasItem.START_ACTION == event.getKey()) {
				runInFxThread(function():Void {running = true;});
			}
			else if(CanvasItem.STOP_ACTION == event.getKey() or CanvasItem.COMPLETE_ACTION == event.getKey()) {
				runInFxThread(function():Void {running = false;});
			}
		} 
	}
	
	override function create() {
		Group {
			content: [
				Rectangle { 
					fill: Color.web("#039bf9")
					width: bind width
					height: bind height
					arcWidth: 3  
				    arcHeight: 3
				}
				Rectangle { 
					fill: Color.web("#dbdbdb")
					width: bind width
					height: bind 0.4 * height
					opacity: 0.3
					arcWidth: 3  
				    arcHeight: 3
				}
				Rectangle {
					layoutX: 2
					layoutY: 2
					fill: Color.TRANSPARENT
					stroke: bind if(selected) Color.web("#ffff00") else Color.TRANSPARENT
					strokeWidth: 2.0 
					width: bind width - 4
					height: bind height - 4
					arcWidth: 3  
					arcHeight: 3
				}
				Label {
					layoutX: 3
					layoutInfo: LayoutInfo { width: 90 }
					graphic: ImageView { image: bind if( running ) ledActive else ledInactive }
					textFill: Color.rgb(0, 0, 0, 0.5)
					text: bind label
					height: bind height
					vpos: VPos.CENTER
					textWrap: false
					font: Font.font("Arial", 9)
				}
			]
			opacity: bind if(dragging) 0.8 else 1
			onMouseClicked: function(e: MouseEvent){
				if(e.button == MouseButton.PRIMARY and e.clickCount == 2 ) {
					AppState.instance.setActiveCanvas(sceneItem);
				}
				else if(e.button == MouseButton.PRIMARY and e.clickCount == 1) {
					if(not isPlaceholder){
						selected = not selected;
						fireTestCaseIconSelected();
					}
				}
			}
			onKeyPressed: function(e: KeyEvent) { 
				if(not isPlaceholder and e.code == KeyCode.VK_DELETE) {
					fireTestCaseRemoved();	
				}
			}
		}
	}
	
	public function remove(): Void {
		fireTestCaseRemoved();
	}
	
	override var revert = false;
	 
	override function toString() { label }
	
	public function copy(): TestCaseIcon {
		copy(false);
	}
	
	public function copy(placeholder: Boolean): TestCaseIcon {
		TestCaseIcon {
			label: label
			selected: selected
			isPlaceholder: placeholder
			sceneItem: sceneItem
			stateListeners: stateListeners
		}
	}
}
