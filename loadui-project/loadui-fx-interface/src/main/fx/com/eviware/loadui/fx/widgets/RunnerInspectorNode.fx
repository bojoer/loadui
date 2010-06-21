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
import com.eviware.loadui.fx.ui.resources.TitlebarPanel;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.RunnerItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.model.ProjectItem;

import com.eviware.loadui.fx.ui.resources.MenuArrow;
import com.eviware.loadui.fx.ui.popup.Menu;
import com.eviware.loadui.fx.ui.popup.PopupMenu;
import com.eviware.loadui.fx.ui.popup.ActionMenuItem;
import javafx.scene.Group; 
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.VPos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.util.Math;

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

import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import javafx.scene.control.ScrollView;
import javafx.scene.control.ScrollBarPolicy;

import com.eviware.loadui.fx.ui.dialogs.*;

public def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.RunnerInspectorNode" );

/**
 * Node to display in the RunnerList representing a RunnerItem.
 */
public class RunnerInspectorNode extends BaseNode, ModelItemHolder, Droppable, EventHandler, TestCaseIconListener, Pagination {
	/**
	 * The RunnerItem to represent.
	 */ 
	public var runner: RunnerItem;
	
	public-init var ghostRunner = false;
	
	override var modelItem = bind lazy runner;
	
	override var itemsPerPage = 4;
	override var fluid = true;
	
	var label:String;
	
	var menuFill: Paint = Color.web("#777777");
	
	protected var width: Number = 135;
	
	protected var height: Number = 195;
	
	public var separatorStroke: Paint = Color.web("#c6c6c6");
	
	var created = false;
	var ready: Boolean = runner.isReady();
	var enabled: Boolean on replace oldVal = newVal {
		if(created and oldVal != newVal) {
			runner.setEnabled( enabled );
		}
	}
	
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
		if(ghostRunner){
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
	
	postinit {
		if(not ghostRunner){
			if(not FX.isInitialized( runner ) )
				throw new RuntimeException( "agent must not be null!" );
			
			runner.addEventListener( BaseEvent.class, this );
			label = runner.getLabel();
			enabled = runner.isEnabled();
		}
		else{
			label = "GHOST AGENT";
		}
	}

	override var accept = function( d: Draggable ) {
		if(not ghostRunner and d.node instanceof TestCaseIcon){
			var tcNode: TestCaseIcon = d.node as TestCaseIcon;
			for(tc in content[x|x instanceof DraggableFrame]){
				if(((tc as DraggableFrame).draggable as TestCaseIcon).label == tcNode.label){
					return false;
				}
			}
			return true;
		}
		else if(ghostRunner and d.node instanceof TestCaseIcon){
			return true;
		}
		else{
			return false;
		}
	}
	
	override var onDrop = function( d: Draggable ) {
		(d.node as TestCaseIcon).remove();
		
		if(not ghostRunner) addTestCase((d.node as TestCaseIcon).copy());
	}
			
	override function handleEvent(e:EventObject) {
		def event = e as BaseEvent;
		if(event.getKey().equals(ModelItem.LABEL)) {
			runInFxThread( function():Void { label = runner.getLabel() } );
		}
		else if(event.getKey().equals(RunnerItem.ENABLED)) {
			runInFxThread( function():Void { 
				ready = runner.isReady();
				enabled = runner.isEnabled();
			});
		}
		else if(event.getKey().equals(RunnerItem.READY)) {
			runInFxThread( function():Void { 
				ready = runner.isReady();
				enabled = runner.isEnabled();
			});
		}
	}
	
	override function create() {
		var toolbarBoxRight: HBox;
		var menuNode:Menu;
		def titleBarPanel = TitlebarPanel {
			backgroundFill: if(ghostRunner) Color.web("#b8b8b8") else Color.web("#d6d6d6")
			content: [
				if(not ghostRunner) OnOffSwitch {
					layoutX: 12
					layoutY: 7
					state: bind enabled with inverse
					managed: false
				} else null, Line {
					endY: 10
					stroke: bind separatorStroke
				},
				if( ghostRunner ) null else menuNode = Menu {
					layoutX: 45
					layoutY: 7
					tooltip: bind label
					contentNode: Group {
						content: [
							Label {
								textFill: bind if( menuNode.menu.isOpen ) Color.WHITE else menuFill
								text: "Menu"
								height: 15
								vpos: VPos.CENTER
								textWrap: false
								font: Font.font("Arial", 9)
							}, MenuArrow {
								fill: bind if( menuNode.menu.isOpen ) Color.WHITE else menuFill
								rotate: 90
								layoutY: 6
								layoutX: 27
							}, Rectangle {
								width: 35
								height: 15
								layoutX: - 3
								layoutY: - 1
								fill: Color.TRANSPARENT
							}
						]
					}
					menu: PopupMenu {
						items: [
							ActionMenuItem {
								text: ##[DELETE]"Rename"
								action: function() { RenameModelItemDialog { modelItem: modelItem } }
							}, ActionMenuItem {
								text: ##[DELETE]"Delete"
								action: function() { DeleteModelItemDialog { modelItem: modelItem } }
							}
						]
					}
				}
				toolbarBoxRight = HBox {
					height: 20
					spacing: 5
					nodeVPos: VPos.CENTER
					vpos: VPos.CENTER
					layoutX: bind ( width - 10 ) - toolbarBoxRight.width
					layoutY: 5
					content: bind [
						Line {
							endY: 10
							stroke: bind separatorStroke
						}, 
						if(not ghostRunner) [ GlowButton {
							padding: 3
							tooltip: "Settings"
							contentNode: FXDNode {
								url: "{__ROOT__}images/component_wrench_icon.fxz"
							}
							action: function() { 
								RunnerConfigurationDialog { runner: runner }.show();
							 }
						}, Line {
							endY: 10
							stroke: bind separatorStroke
						} ] else null, 
						GlowButton {
							padding: 3
							tooltip: "Open Help page"
							contentNode: FXDNode {
								url: "{__ROOT__}images/component_help_icon.fxz"
							}
							action: function() {
								openURL( if(ghostRunner) "http://www.eviware.com" else modelItem.getHelpUrl() );
							}
						}
					]
				}
				Rectangle {
					layoutY: 20
					height: bind height - 20
					width: bind width
					fill: Color.TRANSPARENT
				}
				ImageView {
					layoutX: 8
					layoutY: 25
					image: Image {
                    	url: if(ghostRunner) "{__ROOT__}images/png/ghostrunner_insp_node_background.png" else "{__ROOT__}images/png/runner_insp_node_background.png"
                	}
				}
				Button {
					layoutX: 21
					layoutY: 39
					layoutInfo: LayoutInfo {
						width: 95
						height: 22
					}
					styleClass: "runner-inspector-node-button"
					action: function() { if( page > 0) page--; }
				}
				Button {
					layoutX: 21
					layoutY: 151
					layoutInfo: LayoutInfo {
						width: 95
						height: 22
					}
					styleClass: "runner-inspector-node-button"
					action: function() { if( page < numPages - 1) page++; }
				}
				VBox {
					layoutX: 21
					layoutY: 63
					spacing: 2
					layoutInfo: LayoutInfo {
						width: 96
						height: 86
					}
					vpos: VPos.BOTTOM
					content: bind displayedContent
				}
			]
			titlebarColor: if(ghostRunner) Color.web("#9c9c9c") else Color.web("#b2b2b2")
			titlebarContent: [
				ImageView {
					layoutX: 15
					layoutY: 14
					image: Image {
		            	url: "{__ROOT__}images/png/led-active.png"
		        	}
		        	visible: bind not ghostRunner and enabled and ready 
				}
				ImageView {
					layoutX: 15
					layoutY: 14
					image: Image {
		            	url: "{__ROOT__}images/png/led-inactive.png"
		        	}
		        	visible: bind not ghostRunner and enabled and not ready
				}
				ImageView {
					layoutX: 15
					layoutY: 14
					image: Image {
		            	url: "{__ROOT__}images/png/led-disabled.png"
		        	}
		        	visible: bind not ghostRunner and not enabled
				}
				Label {
					text: bind label.toUpperCase()
					layoutX: bind if(ghostRunner) 15 else 27
					layoutY: 3
					width: bind width - 30
					height: bind 30
					textFill: Color.web("#606060")
					font: Font.font("Arial", 9)
				}
			]
			onMouseWheelMoved: function( e:MouseEvent ) {
				if( e.wheelRotation > 0 and page < numPages - 1 ) {
					page++;
				} else if( e.wheelRotation < 0 and page > 0 ) {
					page--;
				}
			}
		}
		
		created = true;
		
		titleBarPanel;
	}
	
	public function addTestCase(tcNode: TestCaseIcon){
		for(tc in content[x|x instanceof DraggableFrame]){
			if(((tc as DraggableFrame).draggable as TestCaseIcon).label == tcNode.label) {
				//item already in collection
				return;
			}
		}
		var projectItem: ProjectItem = MainWindow.instance.projectCanvas.canvasItem as ProjectItem;
		if(not ghostRunner and projectItem != null and runner != null){
			try{
				projectItem.assignScene( tcNode.sceneItem, runner );
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
				if(not ghostRunner and projectItem != null and runner != null){
					try{
						projectItem.unassignScene( sceneItem, runner );
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
			if(not ghostRunner and unassign and projectItem != null and runner != null){
				try{
					projectItem.unassignScene( ((df as DraggableFrame).draggable as TestCaseIcon).sceneItem, runner );
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
