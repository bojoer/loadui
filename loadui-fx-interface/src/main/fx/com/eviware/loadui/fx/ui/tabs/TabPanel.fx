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


package com.eviware.loadui.fx.ui.tabs;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Container;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.ClipView;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.effect.InnerShadow;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.layout.ClipView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.input.MouseEvent;
import javafx.util.Sequences;
import javafx.scene.input.MouseButton;
import javafx.scene.Scene;

import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

public class TabPanel extends CustomNode {
	
	public var width:Number;
	public var height:Number;
	public var x:Number;
	public var y:Number;
	public var background:Paint;
	public var uniqueNames: Boolean = false;
	
	public var onTabRename: function(tab: ToggleButton): Void;
	public var onTabDeleted: function(tab: ToggleButton): Void;
	public var onTabAdded: function(tab: ToggleButton): Void;
	
	
	var tabBtns:ToggleButton[];
	var tabs:HBox;
	
	var tGroup:ToggleGroup = ToggleGroup{};
	var tabContent:Node;
	var border:Rectangle;
	
	public function addTab(name:String, content:Node): ToggleButton { 
		var tb:ToggleButton;
		
		if ( isUniqueTabName(name) ) 
			insert tb = ToggleButton {
	                layoutInfo: LayoutInfo { width: 100 }
	                text: name
	                toggleGroup: tGroup
	                value: content
	                onMousePressed: function(e: MouseEvent) {
	                	tb.selected = true;
	                }
	                // press delete to delete tab
	                onKeyTyped: function(k:KeyEvent) {
	                	if( k.char == new java.lang.String(java.lang.Character.toChars(0x007f)) ) {
	                		def dialog:Dialog = Dialog {
								title: "Deleting Tab"
								scene: StatisticsWindow.getInstance().scene
								content: Label {
									text: "Delete Tab?"
								}
								okText: "Yes"
								cancelText: "No"
								onOk: function() {
									if ( sizeof tabBtns > 1 ) {
			                			onTabDeleted(tb);
			                			delete tb from tabBtns;
			                			tabBtns[0].selected = true;
			                		}
									dialog.close();
								}
								onCancel: function() {
									dialog.close();
								}
							}
	                		
	                	}
	                }
	                onMouseDragged: function(e: MouseEvent) {
	                	var trans = if ( e.dragAnchorX > e.x )
	                						e.dragX
	                				  else 
	                				  		-e.dragX;
	                	var leftDelta =  (e.dragAnchorX - x) - (Sequences.indexOf(tabBtns, tb) * 100) ;
	                	var rightDelta = (x + width - e.dragAnchorX) - ((tabBtns.size() - Sequences.indexOf(tabBtns, tb) - 1) * 100);
	                	if ( (e.dragAnchorX + trans - leftDelta > x) and (e.dragAnchorX + trans + rightDelta < x + width)) 
	                		tb.translateX = trans;
	                	
	                }
	                onMouseReleased: function(e: MouseEvent) {
	                	if ( tb.translateX != 0 )
		                	for( cnt in [0..tabBtns.size()-1] ) {
		                		if( tb.translateX < 0 ) {
		                			if( javafx.util.Sequences.indexOf(tabBtns, tb) <= cnt )
		                				continue;
			                		if ( tabBtns[cnt].boundsInParent.minX > tb.boundsInParent.minX - 10) {
			                			delete tb from tabBtns;
			                			insert tb before tabBtns[cnt];
			                			break;
			                		}
			                	}
			                	else  {
			                		if( javafx.util.Sequences.indexOf(tabBtns, tb) >= cnt )
		                				continue;
			                		if (tabBtns[cnt].boundsInParent.maxX < tb.boundsInParent.maxX + 10) {
			                			delete tb from tabBtns;
			                			insert tb before tabBtns[cnt];
			                			break;
			                		}
			                	}
		                	}
	                	tb.translateX = 0; 
	                	tb.selected = true; 
	                }
	                onMouseClicked: function(e: MouseEvent) {
							if( e.button == MouseButton.PRIMARY and e.clickCount == 2 ) {
								TabRenameDialog {
								    tabToRename: tb
								    tabButtons: tabBtns
								    uniqueNames: uniqueNames
								    onOk: function(renamedTab: ToggleButton): Void {
								        onTabRename(renamedTab);
								    }
								}.show();
							}
	                }
	            } into tabBtns
	    else
	    	return null;
        
        if (sizeof tabBtns == 1){
        	tb.selected = true;
        }   
        onTabAdded(tb);
        tb;
	}
	
	public override function create():Node {
		Stack {
			styleClass: "tab-panel"
			layoutX: bind x
			layoutY: bind y
			nodeHPos: HPos.LEFT
			nodeVPos: VPos.TOP
			content: [
				border = Rectangle {
					x: 0
					y: 0
					width: bind if( (sizeof tabBtns + 1) * 100 > width )
									(sizeof tabBtns + 1) * 110
								else
									width - 20 
					height: bind height
					arcWidth: 20  
					arcHeight: 20
					fill: background
					stroke: Color.web("#000000")
					strokeWidth: 1
				}, VBox {
					padding: Insets { top: 8 right: 24 bottom: 8 left: 24}
					spacing: 4
					content: [
							tabs = HBox {
								padding: Insets { top: 6 right: 4 bottom: 6 left: 4}
	     						spacing: 6
	     						width: bind width - 20 
	     						content: bind [tabBtns, 
	     									   Button {
	     									   	text: "+"
	     									   	styleClass: "tab-plus"
	     									   	action: function() {
	     									   			addTab("New Tab", null)
	     									   		}
	     									   	}
	     									   ]
							}, Line {
							    startX: 40 
							    startY: bind tabs.height
							    endX: bind if( (sizeof tabBtns + 1) * 100 > width )
												(sizeof tabBtns + 1) * 110
											else
												width - 20 
							    endY: bind tabs.height
							}, tabContent = ClipView {
								clipX: 0
    							clipY: 0
								node: bind tGroup.selectedToggle.value as Node
								pannable: true
							    layoutInfo: LayoutInfo {
							        width: bind width
							        height: bind height
							    }
							}
					]
				}
			]
		}
	}
	
	/**
	* Set content of named tab
	*/
	public function setContent(name:String, content:Node) {
		for( tab in tabBtns ) 
			if( tab.text == name ) {
				tab.value = content;
				break
			}
	}
	
	
	/**
	* return tab names
	*/
	public function getTabNames():String[] {
		var names:String[];
		for( tab in tabBtns ) {
			insert tab.text into names
		}
		names
	}
	
	/**
	* rename tab
	*/
	public function renameTab(oldName:String, newName:String) {
		for( tab in tabBtns ) 
			if( tab.text == oldName ) {
				tab.text = newName;
				break
			}
	}
	
	// remove all tabs
	public function clear() {
		delete tabBtns;
	}
	
	function isUniqueTabName(name:String) {
		for(tab in tabBtns)
			if( tab.text == name )
				return false;
		true
	}
	
} 

