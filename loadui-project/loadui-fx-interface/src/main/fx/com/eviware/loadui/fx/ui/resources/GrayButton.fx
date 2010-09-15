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
*GrayButton.fx
*
*Created on feb 12, 2010, 15:13:46 em
*/

package com.eviware.loadui.fx.ui.resources;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

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

/**
 * A gray button base which can de used when creating buttons. It has three states, NORMAL, PUSHED and HOVER.
 *
 * @author dain.nilsson
 */
public class GrayButton extends CustomNode {
	/**
	 * The width of the button.
	 */
	public var width:Number = 100;
	
	/**
	 * The height of the button.
	 */
	public var height:Number = 100;
	
	/**
	 * Which state the Button is in. 
	 */
	public var state = NORMAL;
	
	/**
	 * Node[] content to place inside the button.
	 */
	public var content: Node[];
	
	override function create() {
		Group {
			translateX: bind if( state == PUSHED ) 1 else 0
			translateY: bind if( state == PUSHED ) 1 else 0
			content: [
				Rectangle {
					visible: bind state != PUSHED
					x: 1
					y: 1
					width: bind width
					height: bind height
					arcHeight: 10
					arcWidth: 10
					fill: Color.BLACK
					opacity: 0.1
				}, Group {
					translateY: bind if( state == HOVER ) -1 else 0
					content: [
						Rectangle {
							width: bind width
							height: bind height
							arcHeight: 10
							arcWidth: 10
							fill: LinearGradient {
								endX: 0
								stops: [
									Stop { offset: 0, color: Color.web("#9d9e9f") },
									Stop { offset: 0.5, color: Color.web("#9a9b9c") },
									Stop { offset: 1, color: Color.web("#8b8c8c") }
								]
							}
							stroke: bind if( state == PUSHED ) null else Color.web("#303030", 0.2)
						}, Group {
							content: bind content
						}
					]
				}, Rectangle {
					visible: bind state == PUSHED
					x: -1
					y: -1
					width: bind width
					height: bind height
					arcHeight: 10
					arcWidth: 10
					fill: Color.BLACK
					opacity: 0.5
					stroke: Color.web("#303030", 0.8)
				}
			]
		}
	}
}
