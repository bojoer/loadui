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
*InspectorButton.fx
*
*Created on feb 12, 2010, 16:04:53 em
*/

package com.eviware.loadui.fx.ui.inspector;

import javafx.scene.CustomNode;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;
import javafx.scene.text.Font;

import com.eviware.loadui.fx.ui.resources.GrayButton;

/**
 * A Button for selecting which Inspector to show on the InspectorPanel.
 *
 * @author dain.nilsson
 */
public class InspectorButton extends CustomNode {
	public var pushed = false on replace {
		button.state = if( pushed ) GrayButton.PUSHED else GrayButton.NORMAL;
	}
	
	/**
	 * The label to put on the button.
	 */
	public var text:String;
	
	/**
	 * The action to perform, when clicked.
	 */
	public var action: function():Void;
	
	def label = Text {
		x: 10
		y: 4
		content: bind text
		textOrigin: TextOrigin.TOP
		font: Font.font("Arial", 10)
	}
	
	def button:GrayButton = GrayButton {
		height: 18
		width: bind label.layoutBounds.width + 20
		content: label
		onMouseEntered: function( e:MouseEvent ) {
			if( not pushed )
				button.state = GrayButton.HOVER;
		}
		onMouseExited: function( e:MouseEvent ) {
			if( not pushed )
				button.state = GrayButton.NORMAL;
		}
		onMousePressed: function( e:MouseEvent ) {
			if( e.primaryButtonDown )
				button.state = GrayButton.PUSHED;
		}
		onMouseReleased: function( e:MouseEvent ) {
			if( not pushed and button.state == GrayButton.PUSHED ) {
				action();
			}
		}
	}
	
	override function create() {
		button
	}
}
