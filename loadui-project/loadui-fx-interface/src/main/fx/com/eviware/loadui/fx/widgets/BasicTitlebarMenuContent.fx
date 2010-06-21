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
*BasicTitlebarMenuContent.fx
*
*Created on mar 18, 2010, 12:36:28 em
*/

package com.eviware.loadui.fx.widgets;

import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Container;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Panel;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.geometry.VPos;
import javafx.util.Math;

import com.eviware.loadui.fx.ui.popup.Menu;
import com.eviware.loadui.fx.ui.popup.MenuItem;
import com.eviware.loadui.fx.ui.popup.PopupMenu;
import com.eviware.loadui.fx.ui.resources.MenuArrow;

public class BasicTitlebarMenuContent extends Container {

	public-init var hasLed = true;

	public var enabled = false;
	
	public var label:String;
	
	public var menuItems:MenuItem[];
	
	public var buttons:Node[];
	
	var menuNode:Node;
	var buttonsNode:Container;
	var menuContent:Node;
	var menu:PopupMenu;
	var led:Circle;
	var labelNode:Label;
	var arrowNode:Node;
	
	override var height = 30;

	postinit {
		content = [
			led = if( hasLed ) Circle {
				radius: 4
				centerX: 12
				fill: bind if( enabled ) Color.RED else Color.GRAY
				strokeWidth: 2
				stroke: Color.BLACK
			} else null, menuNode = Menu {
				tooltip: bind label
				layoutX: if( hasLed ) 22 else 12
				contentNode: Group {
					content: [
						labelNode = Label {
							textFill: bind if( menu.isOpen ) Color.WHITE else Color.web("#303030")
							text: bind label.toUpperCase()
							vpos: VPos.CENTER
							textWrap: false
						}, arrowNode = MenuArrow {
							fill: bind if( menu.isOpen ) Color.WHITE else Color.web("#303030")
							rotate: 90
							layoutY: bind labelNode.height / 2
							layoutX: bind labelNode.width + 5
						}, Rectangle {
							width: bind labelNode.width + arrowNode.layoutBounds.width + 11
							height: bind labelNode.height + 6
							layoutX: -3
							layoutY: -3
							fill: Color.TRANSPARENT
						}
					]
				}
				menu: menu = PopupMenu {
					items: bind menuItems
				}
			}, buttonsNode = HBox {
				content: bind buttons
			}
		];
		requestLayout();
	}

	override function doLayout():Void {
		buttonsNode.width = buttonsNode.getPrefWidth( -1 );
		buttonsNode.height = buttonsNode.getPrefHeight( -1 );
		buttonsNode.layoutX = width - buttonsNode.width;
		buttonsNode.layoutY = ( height - buttonsNode.height ) / 2;
		led.layoutY = height / 2;
		labelNode.width = Math.min( labelNode.getPrefWidth( labelNode.height ), width - ( buttonsNode.width + 40 ) );
		menuNode.layoutY = ( height - labelNode.height ) / 2;
	}
	
	override function getPrefHeight( width:Float ) {
		Math.max(Math.max( menuNode.layoutBounds.height, buttonsNode.getPrefHeight( width ) ), 30)
	}
	
	override function getPrefWidth( height:Float ) {
		30 + labelNode.getPrefWidth( height ) + buttonsNode.getPrefWidth( height )
	}
}
