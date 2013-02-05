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
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.HBox;
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
import com.eviware.loadui.fx.ui.ActivityLed;
import com.eviware.loadui.fx.ui.ConnectingAnimation;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.resources.TitlebarPanel;
import com.eviware.loadui.fx.util.ModelUtils;
import com.eviware.loadui.fx.util.ModelUtils.LabelHolder;
import com.eviware.loadui.api.model.SceneItem;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;

import java.util.EventObject;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;

import java.util.EventObject;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;
import com.eviware.loadui.fx.AppState;

import com.javafx.preview.control.PopupMenu;
import com.javafx.preview.control.MenuItem;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.TestCaseIcon" );

/**
 * Node to display in the AgentList representing a AgentItem.
 */
public class TestCaseIcon extends BaseNode, Draggable, WeakEventHandler {

	// container node instance. Use containerNode.agent to retrieve reference to corresponding AgentItem.
	public var containerNode: AgentItemInspectorNode on replace {
		loaded = containerNode.agent == null or project.isSceneLoaded( sceneItem, containerNode.agent );
	}
	
	public def label = bind labelHolder.label;
	var labelHolder:LabelHolder;
	
	var project:ProjectItem;
	public var sceneItem: SceneItem on replace oldScene {
		oldScene.removeEventListener(ActionEvent.class, this);
		oldScene.getProject().removeEventListener(BaseEvent.class, this);
		sceneItem.addEventListener(ActionEvent.class, this);
		project = sceneItem.getProject();
		loaded = containerNode.agent == null or project.isSceneLoaded( sceneItem, containerNode.agent );
		project.addEventListener(BaseEvent.class, this);
		labelHolder = ModelUtils.getLabelHolder( sceneItem );
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
	
	public var selected: Boolean = false;
	
	public var width: Number = 95;
	
	public var height: Number = 20;
	
	var running: Boolean = sceneItem.isRunning();
	var loaded = false;
	
	override function handleEvent(e: EventObject) {
		if( e.getSource() == sceneItem ) {
			if( e instanceof ActionEvent ) {
				def event = e as ActionEvent;
				if( CanvasItem.START_ACTION.equals( event.getKey() ) ) {
					runInFxThread( function():Void { running = true } );
				} else if( CanvasItem.STOP_ACTION.equals( event.getKey() ) or CanvasItem.COMPLETE_ACTION.equals( event.getKey() ) ) {
					runInFxThread( function():Void { running = false } );
				}
			}
		} else if( e.getSource() == project ) {
			if( e instanceof BaseEvent ) {
				def event = e as BaseEvent;
				if( ProjectItem.SCENE_LOADED.equals( event.getKey() ) ) {
					runInFxThread( function():Void { loaded = containerNode.agent == null or project.isSceneLoaded( sceneItem, containerNode.agent ) } );
				}
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
					graphic: HBox {
						nodeVPos: VPos.CENTER
						content: [
							ActivityLed { active: bind running, disable: bind not containerNode.ready and not isPlaceholder },
							ConnectingAnimation { visible: bind containerNode.ready and not loaded, managed: bind containerNode.ready and not loaded }
						]
					}
					textFill: Color.rgb(0, 0, 0, 0.5)
					text: bind if(containerNode.ready and not loaded) "Distributing..." else label
					height: bind height
					vpos: VPos.CENTER
					textWrap: false
					font: Font.font("Amble",9)
					textOverrun: OverrunStyle.CENTER_ELLIPSES
				},
				contextMenu
			]
			opacity: bind if(dragging) 0.8 else 1
			onMouseClicked: function(e: MouseEvent){
				if(e.button == MouseButton.PRIMARY and e.clickCount == 2 ) {
					AppState.byName("MAIN").setActiveCanvas(sceneItem);
				}
				else if(e.button == MouseButton.PRIMARY and e.clickCount == 1) {
					if(not isPlaceholder){
						selected = not selected;
						fireTestCaseIconSelected();
					}
				}
				else if( e.button == MouseButton.SECONDARY ) {
				   if(not isPlaceholder) {
						contextMenu.show( this, e.screenX, e.screenY );	
					}
				}
			}
			onKeyPressed: function(e: KeyEvent) { 
				if(not isPlaceholder and e.code == KeyCode.VK_DELETE) {
					remove();	
				}
			}
		}
	}
	
	def openAction = MenuItem {
		text: "Open"
		action: function():Void {
			AppState.byName("MAIN").setActiveCanvas(sceneItem);
		}
	}
	
	def unassignAction = MenuItem {
		text: "Unassign"
		action: function():Void {
			remove();
		}
	}

	def contextMenu: PopupMenu = PopupMenu {
		items: [
			openAction,
			unassignAction
		]
		onShowing: function():Void {
			unassignAction.visible = not containerNode.ghostAgent;
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
			selected: selected
			isPlaceholder: placeholder
			sceneItem: sceneItem
			stateListeners: stateListeners
		}
	}
}
