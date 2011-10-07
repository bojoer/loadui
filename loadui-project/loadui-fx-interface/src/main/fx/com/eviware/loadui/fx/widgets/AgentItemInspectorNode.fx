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
 * ProjectNode.fx
 * 
 * Created on feb 10, 2010, 11:47:11 fm
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.input.MouseEvent;
import javafx.util.Sequences;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.pagination.Pagination;
import com.eviware.loadui.fx.ui.resources.DialogPanel;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.model.ProjectItem;

import com.eviware.loadui.fx.ui.resources.MenuArrow;

import javafx.scene.Group; 
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Insets;
import javafx.scene.layout.Container;
import javafx.scene.layout.Stack;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.text.Font;
import javafx.util.Math;
import javafx.util.Sequences;

import com.javafx.preview.control.MenuButton;
import com.javafx.preview.control.MenuItem;

import com.eviware.loadui.fx.dialogs.DeleteModelItemDialog;
import com.eviware.loadui.fx.dialogs.RenameModelItemDialog;
import com.eviware.loadui.fx.widgets.TestCaseIconListener;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.api.model.SceneItem;

import com.eviware.loadui.fx.ui.button.GlowButton;
import javafx.fxd.FXDNode;
import com.eviware.loadui.fx.ui.dnd.DroppableNode;
import com.eviware.loadui.fx.ui.dnd.Droppable;

import java.util.EventObject;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.layout.widgets.OnOffSwitch;
import java.lang.Exception;

import javafx.scene.shape.Rectangle;

import javafx.scene.control.ScrollView;
import javafx.scene.control.ScrollBarPolicy;

import com.eviware.loadui.fx.ui.dialogs.*;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.fx.FxUtils;

public def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.AgentItemInspectorNode" );

/**
 * Node to display in the AgentList representing a AgentItem.
 */
public class AgentItemInspectorNode extends AgentNodeBase, Droppable, TestCaseIconListener, Pagination {
	
	/**
	 * The AgentItem to represent.
	 */ 
	public-init var ghostAgent = false on replace {
		itemsPerPage = if( ghostAgent ) 4 else 3;
		if( ghostAgent ) ready = true;
	}
	
	override var fluid = true;
	
	var menuFill: Paint = Color.web("#777777");
	
	def testCaseListener = TestCaseListener{}
	def workspaceListener = WorkspaceListener{}
	
	def workspace: WorkspaceItem = bind MainWindow.instance.workspace on replace oldVal {
		oldVal.removeEventListener( BaseEvent.class, workspaceListener );
		workspace.addEventListener( BaseEvent.class, workspaceListener );
		isLocalMode = workspace.isLocalMode();
	}
	
	var isLocalMode: Boolean on replace {
		if( ghostAgent ) {
			isNodeActive = isLocalMode;
		}
		checkActivity();
	}

	override var comparator = COMPARE_BY_TOSTRING;
	
	override function selectionChanged(tc: TestCaseIcon): Void { 
	
	}
	
	override function testCaseRemoved(tc: TestCaseIcon): Void {
		if(ghostAgent){
			return;
		}
		tc.removeTestCaseListener(this);
		undeployTestCase(tc.sceneItem);
	}
	
	public function selectTestCaseIcon(sceneItem: SceneItem, selected: Boolean){
		for(df in items[x|x instanceof DraggableFrame]){
			var testCase = (df as DraggableFrame).draggable as TestCaseIcon;
			if(testCase.label == sceneItem.getLabel()){
				testCase.selected = selected;
			}	
			else{
				testCase.selected = false;
			}
			var placeholder = (df as DraggableFrame).placeholder as TestCaseIcon;
			if(placeholder != null){
				placeholder.selected = selected;
			}
		}
	}
	
	public function selectTestCaseIcon(tc: TestCaseIcon){
		selectTestCaseIcon(tc.sceneItem, tc.selected);
	}
	
	public function deselectTestCaseIcons(){
		for(df in items[x|x instanceof DraggableFrame]){
			var testCase = ((df as DraggableFrame).draggable as TestCaseIcon).selected = false;
			var placeholder = (df as DraggableFrame).placeholder as TestCaseIcon;
			if(placeholder != null){
				placeholder.selected = false;
			}
		}
	}

	override var accept = function( d: Draggable ) {
		if(not ghostAgent and d.node instanceof TestCaseIcon){
			var tcNode: TestCaseIcon = d.node as TestCaseIcon;
			for(tc in items[x|x instanceof DraggableFrame]){
				if(((tc as DraggableFrame).draggable as TestCaseIcon).label == tcNode.label){
					return false;
				}
			}
			return true;
		}
		else if(ghostAgent and d.node instanceof TestCaseIcon){
			return true;
		}
		else{
			return false;
		}
	}
	
	override var onDrop = function( d: Draggable ) {
		(d.node as TestCaseIcon).remove();
		
		if(not ghostAgent) addTestCase((d.node as TestCaseIcon).copy());
	}
	
	
	def readEnabled = bind enabled on replace {
		enable = enabled;
	}
	var enable:Boolean = enabled on replace {
		if( enabled != enable )
			agent.setEnabled( enable );
	}
	
	def dummyNode = Rectangle { width: 1, height: 1, managed: false, fill: Color.rgb( 0, 0, 0, 0.001 ) }
	var tcContent:Container;
	def myDisplayedContent = bind displayedItems on replace {
		def dummyIndex = Sequences.indexOf( tcContent.content, dummyNode );
		if( dummyIndex == -1 ) {
			tcContent.content = [ dummyNode, displayedItems ];
		} else {
			for( i in Sequences.reverse([0..(sizeof tcContent.content - 1)]) as Integer[] ) {
				if( i != dummyIndex ) delete tcContent.content[i];
			}
			insert displayedItems into tcContent.content;
		}
	}

	override function create() {
		def base = super.create() as DialogPanel;
		(base.layoutInfo as LayoutInfo).width = 138;
		(base.layoutInfo as LayoutInfo).height = 222;
		
		var toolbarBoxRight: HBox;
		var menuNode:MenuButton;
		if( ghostAgent ) {
			customLabel = "GHOST AGENT";
			enabled = true;
		}
		
		insert [
			HBox {
				hpos: HPos.RIGHT
				nodeHPos: HPos.RIGHT
				layoutInfo: LayoutInfo { height: 14, maxHeight: 14, margin: Insets { bottom: -4 } }
				content: [
					if(not ghostAgent) [
						OnOffSwitch {
							managed: false
							state: bind enable with inverse
						}, Separator {
							vertical: true
						}, menuNode = MenuButton {
							styleClass: bind if( menuNode.showing ) "menu-button-showing" else "menu-button"
							tooltip: Tooltip { text: bind label }
							text: "Menu"
							items: [
								MenuItem {
									text: ##[RENAME]"Rename"
									action: function() { RenameModelItemDialog { labeled: modelItem } }
								}, MenuItem {
									text: ##[DELETE]"Delete"
									action: function() { DeleteModelItemDialog { modelItem: modelItem } }
								}
							]
						}, Separator {
							vertical: true
						} GlowButton {
							padding: 3
							tooltip: "Settings"
							contentNode: FXDNode {
								url: "{__ROOT__}images/component_wrench_icon.fxz"
							}
							action: function() { 
								AgentConfigurationDialog { agent: agent }.show();
							 }
						}
					] else null, Separator {
						vertical: true
					} GlowButton {
						padding: 3
						tooltip: "Open Help page"
						contentNode: FXDNode {
							url: "{__ROOT__}images/component_help_icon.fxz"
						}
						action: function() {
							openURL( if(ghostAgent) "http://www.loadui.org/interface/project-view.html" else modelItem.getHelpUrl() );
						}
					}
				]
			}, VBox {
				padding: Insets { left: 13, top: 14, right: 13, bottom: 18 }
				content: [
					ImageView {
						managed: false
						image: Image {
	                 	url: if(ghostAgent) "{__ROOT__}images/png/ghostagent_insp_node_background.png" else "{__ROOT__}images/png/agent_insp_node_background.png"
	             	}
					}, Button {
						layoutInfo: LayoutInfo {
							width: 95
							height: 22
						}
						graphic: Group {
							content: [
								FXDNode { url: "{__ROOT__}images/agent-arrow-top-active.fxz", visible: bind page != 0 }
								FXDNode { url: "{__ROOT__}images/agent-arrow-top-inactive.fxz", visible: bind page == 0 }
							] 
						}
						vpos: VPos.BOTTOM
						styleClass: "agent-inspector-node-button"
						blocksMouse: false
						action: function() { if( page > 0) page--; }
					}, tcContent = VBox {
						padding: Insets { top: 2, bottom: 2 }
						spacing: 2
						layoutInfo: LayoutInfo {
							height: if( ghostAgent ) 90 else 69
							maxHeight: if( ghostAgent ) 90 else 69
						}
						vpos: VPos.BOTTOM
						content: [ dummyNode, displayedItems ]
					}, Button {
						layoutInfo: LayoutInfo {
							width: 95
							height: 22
						}
						graphic: Group {
							content: [
								FXDNode { url: "{__ROOT__}images/agent-arrow-bottom-active.fxz", visible: bind page < numPages - 1 }
								FXDNode { url: "{__ROOT__}images/agent-arrow-bottom-inactive.fxz", visible: bind page >= numPages - 1 }
							] 
						}
						vpos: VPos.BOTTOM
						styleClass: "agent-inspector-node-button"
						blocksMouse: false
						action: function() { if( page < numPages - 1) page++; }
					}, if( ghostAgent ) null else HBox {
						padding: Insets { top: 8, left: 2 }
						spacing: 2
						vpos: VPos.CENTER
						nodeVPos: VPos.CENTER
						layoutInfo: LayoutInfo {
							width: 96
							height: 15
						}
						content: [
							Label {
								text: "ACT"
								font: Font { size: 10 }
								tooltip: Tooltip { text: "Activity" }
							}, activityNode, Group {
								content: [
									Rectangle {
										width: 24
										height: 11
									}, Label {
										text: bind "{utilization}%"
										tooltip: Tooltip { text: "Agenty Activity: {utilization}%" }
										textFill: bind if( ready ) Color.rgb( 0x0, 0xce, 0x16 ) else Color.rgb( 0x8c, 0x8c, 0x8c )
										font: Font { size: 10 }
										hpos: HPos.CENTER
										layoutInfo: LayoutInfo { width: 24, height: 11 }
									}
								]
							}
						]
					}
				]
			}
		] into (base.body as Container).content;
		
		base;
	}
	
	public function addTestCase(tcNode: TestCaseIcon){
		for(tc in items[x|x instanceof DraggableFrame]){
			if(((tc as DraggableFrame).draggable as TestCaseIcon).label == tcNode.label) {
				//item already in collection
				return;
			}
		}
		var projectItem: ProjectItem = MainWindow.instance.projectCanvas.canvasItem as ProjectItem;
		if(not ghostAgent and projectItem != null and agent != null){
			try{
				projectItem.assignScene( tcNode.sceneItem, agent );
			}
			catch(e: Exception){
				e.printStackTrace();
			}
		}
		tcNode.sceneItem.addEventListener(BaseEvent.class, testCaseListener);
		tcNode.addTestCaseListener(this);
		insert DraggableFrame{
			draggable: tcNode
			placeholder: tcNode.copy(true)
		} 
		into items;
		tcNode.containerNode = this;
		checkActivity();
	}
	
	public function undeployTestCase(sceneItem: SceneItem){
		var projectItem: ProjectItem = MainWindow.instance.projectCanvas.canvasItem as ProjectItem;
		for(df in items[x|x instanceof DraggableFrame]){
			if(((df as DraggableFrame).draggable as TestCaseIcon).label == sceneItem.getLabel()){
				if(not ghostAgent and projectItem != null and agent != null){
					try{
						projectItem.unassignScene( sceneItem, agent );
					}
					catch(e: Exception) {
						//do nothing, already unassigned
					}
				}
				sceneItem.removeEventListener(BaseEvent.class, testCaseListener);
				delete df as Node from items;
				return;
			}	
		}
		checkActivity();
	}

	public function clearTestCases(unassign: Boolean){
		var projectItem: ProjectItem = MainWindow.instance.projectCanvas.canvasItem as ProjectItem;
		for(df in items[x|x instanceof DraggableFrame]){
			def scene: SceneItem = ((df as DraggableFrame).draggable as TestCaseIcon).sceneItem;
			if(not ghostAgent and unassign and projectItem != null and agent != null){
				try{
					projectItem.unassignScene( scene, agent );
				}
				catch(e: Exception) {
 					//do nothing, already unassigned
				}
			}
			scene.removeEventListener(BaseEvent.class, testCaseListener);
		} 
		delete items;
		checkActivity();
	}
	
	function checkActivity() {
		// should blink only if at least one test case is active and in local
		// mode and ghost agent or dist mode and not ghost agent
		var shouldBlink = false;
		if( ( isLocalMode and ghostAgent) or ( not isLocalMode and not ghostAgent ) ) {
			for(d in items){
				if(((d as DraggableFrame).draggable as TestCaseIcon).sceneItem.isActive()){
					shouldBlink = true;
					break;	
				}
			}
		}
		if(blink == not shouldBlink){
			blink = shouldBlink;
		}
	}
	
	override function toString() { label }
	
	postinit{
		blocksMouse = true;		
	}
	
}

class TestCaseListener extends EventHandler {
	override function handleEvent( e: EventObject ) { 
		if( e instanceof BaseEvent ) {
			def event = e as BaseEvent;
			if( CanvasObjectItem.ACTIVITY.equals( event.getKey() ) ){
				FxUtils.runInFxThread( function():Void {
					checkActivity();
				} );
			}
		}
	}
}

class WorkspaceListener extends EventHandler {
	override function handleEvent( e:EventObject ) { 
		if(e.getSource() == workspace){
			if( e instanceof PropertyEvent ) {
				def event = e as PropertyEvent;
				if( WorkspaceItem.LOCAL_MODE_PROPERTY == event.getProperty().getKey() ) {
					FxUtils.runInFxThread( function():Void {
						isLocalMode = workspace.isLocalMode();
					} );
				} 
			}
		} 
	}
}
