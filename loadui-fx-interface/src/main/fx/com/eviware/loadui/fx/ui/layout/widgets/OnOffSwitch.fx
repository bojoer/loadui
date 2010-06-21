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
*OnOffSwitch.fx
*
*Created on apr 15, 2010, 10:00:54 fm
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

import com.eviware.loadui.fx.ui.dnd.SliderNode;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import com.eviware.loadui.fx.ui.layout.Widget;

/**
 * On/Off Widget which controls a boolean value.
 *
 * @author dain.nilsson
 */
public class OnOffSwitch extends BaseNode, Resizable, TooltipHolder {
	
	def ACTIVE_FILL = Color.BLACK;
	def INACTIVE_FILL = Color.rgb( 0x2d, 0x2d, 0x2d );
	def STROKE1 = Color.rgb( 0xa0, 0xa0, 0xa0 );
	def STROKE2 = Color.rgb( 0x53, 0x53, 0x53 );
	
	override var width = 40;
	override var height = 20;
	
	var noUpdate = false;
	public var state:Boolean = false on replace {
		noUpdate = true;
		switchHandle.selectedIndex = if( state ) 0 else 1;
		noUpdate = false;
	}
	def selectedIndex = bind switchHandle.selectedIndex on replace {
		if( not noUpdate )
			state = selectedIndex == 0;
	}
	
	def switchBase = Group {
		content: [
			Rectangle {
				x: 1
				width: 11
				height: 19
				fill: Color.rgb( 0x0, 0x0, 0x0, 0.3 )
			}, Rectangle {
				y: 1
				width: 11
				height: 19
				fill: Color.rgb( 0xff, 0xff, 0xff, 0.7 )
			}, Rectangle {
				x: 1
				y: 1
				width: 10
				height: 18
				fill: Color.rgb( 0x0, 0x0, 0x0, 0.7 )
			}, Rectangle {
				x: 2
				y: 2
				width: 8
				height: 16
				fill: Color.rgb( 0x0, 0x0, 0x0 )
			}
		]
	}
	
	def switchHandle = SliderNode {
		layoutInfo: LayoutInfo { height: 18 }
		numOptions: 2
		contentNode: Group {
			content:[
				Rectangle {
					x: 1
					y: 1
					width: 10
					height: 10
					fill: Color.rgb( 0x42, 0x42, 0x42 )
				}, Rectangle {
					x: 2
					y: 2
					width: 8
					height: 8
					fill: Color.rgb( 0x6b, 0x6b, 0x6b )
					arcWidth: 4
					arcHeight: 4
				}, Line {
					startX: 4
					endX: 8
					layoutY: 3
					stroke: STROKE1
				}, Line {
					startX: 4
					endX: 8
					layoutY: 4
					stroke: STROKE2
				}, Line {
					startX: 4
					endX: 8
					layoutY: 5
					stroke: STROKE1
				}, Line {
					startX: 4
					endX: 8
					layoutY: 6
					stroke: STROKE2
				}, Line {
					startX: 4
					endX: 8
					layoutY: 7
					stroke: STROKE1
				}, Line {
					startX: 4
					endX: 8
					layoutY: 8
					stroke: STROKE2
				}
			]
		}
	}
	
	def onLabel = Label {
		text: "ON"
		layoutX: 15
		layoutY: -2
		textFill: bind if( state ) ACTIVE_FILL else INACTIVE_FILL
		font: Font {
			size: 10
			embolden: true
		}
	}
	
	def offLabel = Label {
		text: "OFF"
		layoutX: 15
		layoutY: 6
		textFill: bind if( state ) INACTIVE_FILL else ACTIVE_FILL
		font: Font {
			size: 10
			embolden: true
		}
	}
	
	override var blocksMouse = true;
	
	override function create() {
		switchHandle.width = switchHandle.getPrefWidth(-1);
		
		Group {
			content: Group {
				layoutX: bind (width - 40) / 2
				layoutY: bind (height - 20) / 2
				content: [
					switchBase,
					switchHandle,
					onLabel,
					offLabel
				]
			}
		}
	}
	
	override function getPrefWidth( height:Float ) {
		40
	}
	
	override function getPrefHeight( width:Float ) {
		20
	}
}
