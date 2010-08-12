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
import javafx.scene.layout.LayoutInfo;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.control.Tooltip;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.geometry.Insets;
import javafx.util.Math;

import com.javafx.preview.control.MenuButton;

import com.eviware.loadui.fx.ui.popup.Menu;
import com.eviware.loadui.fx.ui.popup.MenuItem;
import com.eviware.loadui.fx.ui.popup.PopupMenu;
import com.eviware.loadui.fx.ui.resources.MenuArrow;

import com.eviware.loadui.fx.FxUtils;

public class BasicTitlebarMenuContent extends HBox {

	public-init var hasLed = true;
	public var enabled = false;
	
	public var label:String;
	public var tooltip:String;
	
	public var menuItems:Node[] on replace {
		menuNode.items = menuItems;
	}
	
	public var buttons:Node[];
	
	override var layoutInfo = LayoutInfo { height: 30 };
	override var padding = Insets { left: 5 };
	override var nodeVPos = VPos.CENTER;
	override var spacing = 3;
	
	var menuNode:MenuButton;
	var buttonsNode:Container;
	var menuContent:Node;
	var led:Circle;

	init {
		content = [
			led = if( hasLed ) Circle {
				radius: 4
				centerX: 12
				fill: bind if( enabled ) Color.RED else Color.GRAY
				strokeWidth: 2
				stroke: Color.BLACK
			} else null, menuNode = MenuButton {
				styleClass: bind if( menuNode.showing ) "menu-button-showing" else "menu-button"
				text: bind label.toUpperCase();
				items: menuItems
				tooltip: Tooltip { text: bind if(FX.isInitialized(tooltip)) tooltip else label }
			}, buttonsNode = HBox {
				content: bind buttons
			}
		];
		requestLayout();
	}
}
