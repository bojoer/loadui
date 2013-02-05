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
*TitlebarPanel.fx
*
*Created on feb 12, 2010, 13:02:12 em
*/

package com.eviware.loadui.fx.ui.resources;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.effect.Effect;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.resources.Paints;

public class TitlebarPanel extends CustomNode {
	public var titlebarColor:Color = Color.web("#999999");
	
	public var backgroundFill:Paint = Color.WHITE;
	
	public var content:Node[] = [];
	
	public var titlebarContent:Node[] = [];
	
	def target = Group {
		content: bind content
		layoutY: 30
	}
	
	public var titlebarEffect:Effect;
	
	public def titlebar = BaseNode {
		contentNode: Group {
			effect: bind titlebarEffect;
			content: [
				Rectangle {
					fill: titlebarColor
					width: bind target.layoutBounds.width
					height: 31
				}, Rectangle {
					width: bind target.layoutBounds.width
					height: 18
					fill: LinearGradient {
						endX: 0
						stops: [
							Stop { offset: 0, color: Color.rgb( 0xff, 0xff, 0xff, 0.3 ) }
							Stop { offset: 0.24, color: Color.rgb( 0xff, 0xff, 0xff, 0.7 ) }
							Stop { offset: 0.25, color: Color.rgb( 0xff, 0xff, 0xff, 0.5 ) }
							Stop { offset: 1, color: Color.rgb( 0xff, 0xff, 0xff, 0.1 ) }
						]
					}
				}, Group {
					content: bind titlebarContent
				}
			]
		}
	}
	
	override function create() {
		Group {
			content: [
				Group {
					clip: Rectangle {
						width: bind target.boundsInLocal.width
						height: bind target.boundsInLocal.height + 30
						arcWidth: 20
						arcHeight: 20
					}
					content: [
						Rectangle {
							width: bind target.boundsInLocal.width
							height: bind target.boundsInLocal.height + 30
							fill: bind backgroundFill
						},
						titlebar,
						target,
						Rectangle {
							fill: Color.TRANSPARENT
							stroke: Color.BLACK
							strokeWidth: 3
							opacity: 0.3
							width: bind target.boundsInLocal.width + 10
							height: bind target.boundsInLocal.height + 40
							arcWidth: 20
							arcHeight: 20
							x: -11
							y: -11
						}, Rectangle {
							fill: Color.LIGHTGRAY
							opacity: 0.8
							width: 3
							height: bind target.boundsInLocal.height + 30
						}
					]
				}, Rectangle {
					arcWidth: 20
					arcHeight: 20
					width: bind target.boundsInLocal.width
					height: bind target.boundsInLocal.height + 30
					fill: Color.TRANSPARENT
					stroke: Color.BLACK
					strokeWidth: 2
				}
			]
		}
	}
}
