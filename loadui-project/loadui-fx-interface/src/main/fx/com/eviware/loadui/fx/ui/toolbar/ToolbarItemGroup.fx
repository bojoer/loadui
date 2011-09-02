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
/*
*ToolbarItemGroup.fx
*
*Created on mar 15, 2010, 10:53:55 fm
*/

package com.eviware.loadui.fx.ui.toolbar;


import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.TextOrigin;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Polygon;
import javafx.scene.control.Button;
import javafx.scene.layout.LayoutInfo;

import java.lang.Math;

/**
 * Graphical node used by the Toolbar to represent a group of ToolbarItemNodes. 
 *
 * @author dain.nilsson
 */
public class ToolbarItemGroup extends CustomNode {

	override var styleClass = "toolbar-item-group";
	
	/**
	 * The background color of expander button 
	 */
	public var expanderButtonBackgroundFill: Paint = Color.rgb( 0x60, 0x60, 0x60 );
	
	/**
	 * The background color of expander button arrow
	 */
	public var expanderButtonArrowFill: Paint = Color.rgb( 0x22, 0x22, 0x22 );
	
	/**
	 * Text color 
	 */
	public var textFill: Paint = Color.rgb( 0x9c, 0x9c, 0x9c );
	
	public var font:Font;
	
	/**
	 * Become true when mouse enters expanderButton and false when exits it. 
	 * This is used to change style on hover.
	 */
	public var expanderButtonHover: Boolean = false;
	
	/**
	 * A ToolbarExpander used to place the ToolbarItemNodes in this ToolbarGroup into when in an expanded state.
	 */
	public-init var expandedGroup:ToolbarExpander;
	
	/**
	* The height in pixels of the item group.
	*/
	public-init var groupHeight:Number;
	
	/**
	* The upper margin of the item group.
	*/
	public-init var topMargin:Number;
	
	public var width:Number = 109;
	
	public var showLabels = true;
	
	/**
	* The left margin of the item group.
	*/
	public var leftMargin:Integer = 13;

	def frame = ToolbarItemFrame { leftMargin: leftMargin, showLabels: showLabels }
	
	var expanderButtonHeight = Math.max( groupHeight/3, 18 );
	
	/**
	 * The ToolbarItemNodes contained in this ToolbarGroup.
	 */
	public var items: ToolbarItemNode[] on replace {
		if( sizeof items == 0 ) {
			clearFrame();
		} else {
			setupFrame();
		}
	}
	
	/**
	 * The category of the ToolbarItemNodes in this ToolbarItemGroup.
	 */
	public-init var category:String;
	var label:String;
	
	function expand():Void {
		delete collapsedGroup from group.content;
		clearFrame();
		expandedGroup.group = this;
	}
	
	package function collapse():Void {
		setupFrame();
		insert collapsedGroup into group.content;
	}
	
	function clearFrame() {
		frame.item = null;
	}
	
	function setupFrame() {
		def item = items[0];
		label = item.label;
		frame.item = item;
	}
	
	var btn:Button;
	def collapsedGroup:Group = Group {
		content: [
			Text {
				x: leftMargin
				y: topMargin
				content: category
				textOrigin: TextOrigin.TOP
				font: bind font
				fill: bind textFill
			},
			frame,
			btn = Button {
				styleClass: "expander-button"
				graphic: Polygon {
					fill: bind if(btn.hover) Color.web("#222222") else Color.web("#8b8b8b")
					points: [
						0, 0,
						4, 5,
						4, 5,
						0, 10
					]
				}
				layoutInfo: LayoutInfo { width: 20, height: expanderButtonHeight } //{ width: 24, height: 35 }
				layoutX: bind width - 20
				layoutY: groupHeight / 2 - expanderButtonHeight / 2
				visible: bind sizeof items > 1
				action: expand
			}
		]
	}
	
	var group:Group;
	
	override function create() {
		group = Group {
			layoutY: -12
			content: collapsedGroup
		}
	}
	
	override function toString():String {
		category
	}

}
