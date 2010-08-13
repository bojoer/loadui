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
import com.eviware.loadui.api.model.AgentItem;
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

public def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.AgentInspectorNode" );

/**
 * Node to display in the AgentList representing a AgentItem.
 */
public class AgentInspectorNode extends AgentNodeBase, Droppable, TestCaseIconListener, Pagination {
	/**
	 * The AgentItem to represent.
	 */ 
	
	public-init var ghostAgent = false on replace {
		itemsPerPage = if( ghostAgent ) 4 else 3;
	}
	
	override var fluid = true;
	
	
	var menuFill: Paint = Color.web("#777777");
	
	var contentChanging = false;
	override var content on replace {
		if( not contentChanging ) {
			contentChanging = true;
				content = Sequences.sort( content, COMPARE_BY_TOSTRING ) as Node[];
			contentChanging = false;
		}
	}
	
	override function selectionChanged(tc: TestCaseIcon): Void { 
	
	}
	
	override function testCaseRemoved(tc: TestCaseIcon): Void {
		if(ghostAgent){
			return;
		}
		undeployTestCase(tc.sceneItem);
	}
	
	public function selectTestCaseIcon(sceneItem: SceneItem, selected: Boolean){
		for(df in content[x|x instanceof DraggableFrame]){
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
		for(df in content[x|x instanceof DraggableFrame]){
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
			for(tc in content[x|x instanceof DraggableFrame]){
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

	override function create() {
		def base = super.create() as DialogPanel;
		(base.layoutInfo as LayoutInfo).width = 138;
		(base.layoutInfo as LayoutInfo).height = 222;
		
		var toolbarBoxRight: HBox;
		var menuNode:MenuButton;
		if( ghostAgent ) {
			(base.body as Container).content = Label { text: "GHOST AGENT", layoutInfo: LayoutInfo { margin: Insets { left: 6 } } };
		}
		
		insert [
			HBox {
				hpos: HPos.RIGHT
				nodeHPos: HPos.RIGHT
				layoutInfo: LayoutInfo { height: 10, maxHeight: 10 }
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
									action: function() { RenameModelItemDialog { modelItem: modelItem } }
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
						styleClass: "agent-inspector-node-button"
						action: function() { if( page > 0) page--; }
					}, VBox {
						padding: Insets { top: 2, bottom: 2 }
						spacing: 2
						layoutInfo: LayoutInfo {
							height: if( ghostAgent ) 90 else 69
							maxHeight: if( ghostAgent ) 90 else 69
						}
						vpos: VPos.BOTTOM
						content: bind displayedContent
					}, Button {
						layoutInfo: LayoutInfo {
							width: 95
							height: 22
						}
						styleClass: "agent-inspector-node-button"
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
							}, Group {
								content: [
									Rectangle {
										width: 44
										height: 11
									}, ImageView {
										layoutX: 2
										layoutY: 2
										image: Image { url: "{__ROOT__}images/png/agent-cpu-inactive.png" }
									}, ImageView {
										layoutX: 2
										layoutY: 2
										image: Image { url: "{__ROOT__}images/png/agent-cpu-active.png" }
										viewport: bind Rectangle2D {
											width: 4*utilization/10 as Integer
											height: 8
										}
										visible: bind utilization > 0
									}
								]
							}, Group {
								content: [
									Rectangle {
										width: 24
										height: 11
									}, Label {
										text: bind "{utilization}%"
										tooltip: Tooltip { text: "Agenty Activity: {utilization}%" }
										textFill: Color.rgb( 0x0, 0xce, 0x16 )
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
		for(tc in content[x|x instanceof DraggableFrame]){
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
		tcNode.addTestCaseListener(this);
		insert DraggableFrame{
			draggable: tcNode
			placeholder: tcNode.copy(true)
		} 
		into content;
	}
	
	public function undeployTestCase(sceneItem: SceneItem){
		var projectItem: ProjectItem = MainWindow.instance.projectCanvas.canvasItem as ProjectItem;
		for(df in content[x|x instanceof DraggableFrame]){
			if(((df as DraggableFrame).draggable as TestCaseIcon).label == sceneItem.getLabel()){
				if(not ghostAgent and projectItem != null and agent != null){
					try{
						projectItem.unassignScene( sceneItem, agent );
					}
					catch(e: Exception) {
						//do nothing, already unassigned
					}
				}
				delete df as Node from content;
				return;
			}	
		}
	}
	
	public function clearTestCases(unassign: Boolean){
		var projectItem: ProjectItem = MainWindow.instance.projectCanvas.canvasItem as ProjectItem;
		for(df in content[x|x instanceof DraggableFrame]){
			if(not ghostAgent and unassign and projectItem != null and agent != null){
				try{
					projectItem.unassignScene( ((df as DraggableFrame).draggable as TestCaseIcon).sceneItem, agent );
				}
				catch(e: Exception) {
 					//do nothing, already unassigned
				}
			}
		} 
		delete content;
	}
					
	override function toString() { label }
}
