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
*ActionMenuItem.fx
*
*Created on feb 11, 2010, 16:48:52 em
*/

package com.eviware.loadui.fx.ui.popup;

import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;

/**
 * A basic MenuItem with a label that performs some action when clicked.
 *
 * @author dain.nilsson
 */
public class ActionMenuItem extends MenuItem {
	/**
	 * The test to display in the PopupMenu.
	 */
	public var text:String;
	
	/**
	 * The action to perform when this MenuItem is chosen.
	 */
	public var action: function():Void;
	
	/**
	 * Activate the action.
	 */
	public function activate():Void {
		PopupMenu.closeAll();
		action();
	}
	
	def label = Text {
		content: bind text
		textOrigin: TextOrigin.TOP
		fill: bind if( selected ) Color.WHITE else Color.BLACK
		x: 5
		y: 3
	}
	
	override function create() {
		Group {
			content: [
				Rectangle {
					fill: bind if(selected) Color.web("#3e5fc3") else Color.WHITE
					width: bind width
					height: bind height
				}, label
			]
			onMouseClicked: function( e:MouseEvent ) {
				if( e.button == MouseButton.PRIMARY ) {
					activate();
				}
			}
		}
	}
	
	override function getPrefHeight( width:Float ) {
		label.layoutBounds.height + 6
	}
	
	override function getPrefWidth( height:Float ) {
		label.layoutBounds.width + 10
	}
}
