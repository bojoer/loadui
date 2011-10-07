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
*InspectorButton.fx
*
*Created on feb 12, 2010, 16:04:53 em
*/

package com.eviware.loadui.fx.ui.inspector;

import javafx.scene.CustomNode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;
import javafx.scene.text.Font;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import com.sun.javafx.scene.layout.Region;

/**
 * A Button for selecting which Inspector to show on the InspectorPanel.
 *
 * @author dain.nilsson
 * @author henrik.olsson
 */
 
/**
 * The normal state.
 */
public def NORMAL = 0;

/**
 * The pushed state.
 */
public def PUSHED = 1;

/**
 * The state for when the cursor is hovering over the button.
 */
public def HOVER = 2;

public class InspectorButton extends CustomNode {
	public var pushed = false on replace {
		state = if( pushed ) PUSHED else NORMAL;
	}
	
	/**
	 * The width of the button.
	 */
	public var width:Number = bind label.layoutBounds.width + 25;
	
	/**
	 * The height of the button.
	 */
	public var height:Number = 25;
	
	/**
	 * Which state the Button is in. 
	 */
	public var state = NORMAL;
	
	/**
	 * Node[] content to place inside the button.
	 */
	public var content: Node[];
	
	/**
	 * The label to put on the button.
	 */
	public var text:String;
	
	/**
	 * The action to perform, when clicked.
	 */
	public var action: function():Void;
	
	def label = Text {
		x: 12
		y: 7
		content: bind text
		font: Font.font("Amble", 10)
		fill: bind if( state == PUSHED ) Color.web("#cbcbcb") else Color.web("#202020") 
		textOrigin: TextOrigin.TOP
	}
	
	init {
		insert label into content;		
	}
	
	override def onMouseEntered = function ( e:MouseEvent ) {
		if( not pushed )
			state = HOVER;
	};
	
	override def onMouseExited = function ( e:MouseEvent ):Void {
		if( not pushed )
			state = NORMAL;
	};
	
	override def onMousePressed = function ( e:MouseEvent ):Void {
		if( e.primaryButtonDown )
		{
			state = PUSHED;
			action();
		}
	};
	
	override function create() {
		Group {
			content: [
				
				Group {
					content: [
						Region {
							managed: true
							width: bind width
							height: bind height
							style: bind
								if(state == PUSHED )
									"-fx-background-insets: 0 0 1 0, 0 1 1 1, 1 1 0 1; -fx-background-color: #282828, #cccccc, #6f6f6f;"
								else
									"-fx-background-insets: 0 0 0 0, 0 1 1 1, 1 1 1 1; -fx-background-color: #282828, #9c9c9c, #555555;"
						}, Group {
							content: bind content
						}
					]
				}
			]
		}
	}
}
