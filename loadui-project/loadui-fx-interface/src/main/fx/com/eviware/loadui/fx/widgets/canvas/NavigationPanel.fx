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
*NavigationPanel.fx
*
*Created on feb 26, 2010, 08:51:24 fm
*/

package com.eviware.loadui.fx.widgets.canvas;

import javafx.util.Math;
import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextOrigin;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.fxd.FXDNode;
import javafx.animation.transition.TranslateTransition;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.dnd.MovableNode;

/**
 * Displays a minimap for a Canvas, which allows the user to navigate the full Canvas area.
 *
 * @author dain.nilsson
 */
public class NavigationPanel extends CustomNode, Resizable {
	/**
	 * The Canvas to control.
	 */
	public var canvas:Canvas;
	
	def cox = bind canvas.offsetX on replace {
		if( not handle.dragging ) handle.layoutX = Math.max( 0, scale * cox );
	}
	
	def coy = bind canvas.offsetY on replace {
		if( not handle.dragging ) handle.layoutY = Math.max( 0, scale * coy );
	}
	
	def scale = bind Math.min( ( width - 32 ) / canvas.areaWidth, ( height - 58 ) / canvas.areaHeight );
	
	def components = bind canvas.components on replace {
		refreshMinis();
	}
	
	def miniatures = Group {};
	function refreshMinis() {
		miniatures.content = null;
		for( component in components ) {
			insert Miniature {
				layoutX: bind component.layoutX * scale
				layoutY: bind component.layoutY * scale
				width: bind component.layoutBounds.width * scale
				height: bind component.layoutBounds.height * scale
				fill: bind component.color
			} into miniatures.content;
		}
	}
	
	var realHeight = height;
	
	var hidden = false on replace {
		if( hidden ) {
			collapseTransition.toY = height - 26;
			collapseTransition.action = function() { delete innerContent from outerContent.content; realHeight = 26 }
			collapseTransition.playFromStart();
		} else {
			realHeight = height;
			insert innerContent into outerContent.content;
			collapseTransition.toY = 0;
			collapseTransition.action = null;
			collapseTransition.playFromStart();
		}
	}
	
	def collapseTransition = TranslateTransition {
		node: this
		duration: 100ms
	}

	var map:Rectangle;
	var handle:MovableNode;
	var innerContent:Group;
	var outerContent:Group;
	
	override function create() {
		outerContent = Group {
			blocksMouse: true
			content: [
				Rectangle {
					width: bind width - 1
					height: bind realHeight
					fill: Color.rgb( 0x90, 0x90, 0x90, 0.95 )
					arcWidth: 10
					arcHeight: 10
				}, Rectangle {
					width: bind width - 1
					height: 5
					layoutY: bind realHeight - 5
					fill: Color.rgb( 0x90, 0x90, 0x90, 0.95 )
				}, Rectangle {
					layoutX: 1
					layoutY: 1
					width: bind width - 1
					height: bind realHeight - 1
					fill: Color.rgb( 0x6d, 0x6d, 0x6d )
					arcWidth: 10
					arcHeight: 10
				}, Rectangle {
					layoutX: 1
					layoutY: bind realHeight - 4
					width: bind width - 1
					height: 5
					fill: Color.rgb( 0x6d, 0x6d, 0x6d )
				}, Group {
					content: [
						Rectangle {
							layoutX: 2
							layoutY: 2
							width: bind width - 4
							height: 22
							arcWidth: 10
							arcHeight: 10
							fill: Color.web( "#4f4f4f" )
							stroke: Color.web( "#4f4f4f", 0.5 )
						}, Rectangle {
							layoutX: 2
							layoutY: 20
							width: bind width - 4
							height: 5
							fill: Color.web( "#4f4f4f" )
							stroke: Color.web( "#4f4f4f", 0.5 )
						}, Stack {
							layoutY: 3
							cursor: Cursor.HAND
							width: bind 30
							layoutInfo: LayoutInfo { hpos: HPos.LEFT }
							content: [
								Rectangle {
									width: 30
									height: 20
									fill: Color.TRANSPARENT
								}, FXDNode {
									url: "{__ROOT__}images/double_arrows.fxz"
									scaleY: bind if( not hidden ) -1 else 1
								}
							]
							blocksMouse: true
							onMouseClicked: function( e:MouseEvent ) {
								if( e.button == MouseButton.PRIMARY )
									hidden = not hidden;
							}
						}, Text {
							textOrigin: TextOrigin.TOP
							layoutX: 30
							layoutY: 8
							content: "Navigation"
							fill: Color.web("#303030")
							layoutInfo: LayoutInfo { hpos: HPos.LEFT, vpos: VPos.CENTER }
						}
					]
					onMouseClicked: function( e:MouseEvent ) {
						if( e.button == MouseButton.PRIMARY and e.clickCount == 2 )
							hidden = not hidden;
					}
				}, innerContent = Group {
					content: [
						Rectangle {
							layoutX: 16
							layoutY: 35
							width: bind width - 32
							height: 6
							arcWidth: 10
							arcHeight: 6
							fill: Color.web( "#262626" )
						}, Rectangle {
							layoutX: 16
							layoutY: 38
							width: bind width - 32
							height: bind height - 58
							fill: Color.web( "#262626" )
						}, Group {
							layoutX: bind ( width - map.width ) / 2
							layoutY: bind 38 + ( ( height - 58 ) - map.height) / 2
							clip: Rectangle {
								width: bind canvas.areaWidth * scale
								height: bind canvas.areaHeight * scale
							}
							content: [
								map = Rectangle {
									width: bind canvas.areaWidth * scale
									height: bind canvas.areaHeight * scale
									fill: Color.web( "#2d2d2d" )
								}, miniatures,
								handle = MovableNode {
									contentNode: Group {
										content: [
											Rectangle {
												width: bind canvas.width * scale
												height: bind canvas.height * scale
												fill: Color.rgb( 0x0, 0x70, 0xff, 0.18 )
											}, Rectangle {
												width: 2
												height: bind canvas.height * scale
												fill: Color.rgb( 0x2c, 0x57, 0xfe )
											}, Rectangle {
												x: bind canvas.width * scale - 2
												width: 2
												height: bind canvas.height * scale
												fill: Color.rgb( 0x2c, 0x57, 0xfe )
											}, Rectangle {
												width: bind canvas.width * scale
												height: 2
												fill: Color.rgb( 0x2c, 0x57, 0xfe )
											}, Rectangle {
												y: bind canvas.height * scale - 2
												width: bind canvas.width * scale
												height: 2
												fill: Color.rgb( 0x2c, 0x57, 0xfe )
											}
										]
									}
									onGrab: function() {
										handle.containment = map.localToScene( map.layoutBounds );
									}
									onDragging: function() {
										canvas.offsetX = ( handle.layoutX + handle.translateX ) / scale as Integer;
										canvas.offsetY = ( handle.layoutY + handle.translateY ) / scale as Integer;
										canvas.refreshTerminals();
									}
								}
							]
						}, FXDNode {
							layoutX: 18
							layoutY: 38
							url: "{__ROOT__}images/glare.fxz"
						}
					]
				}
			]
		}
	}
	
	override function getPrefWidth( height:Float ) {
		width
	}
	
	override function getPrefHeight( width:Float ) {
		height
	}
}

class Miniature extends CustomNode {
	public var fill:Paint;
	public var width:Number;
	public var height:Number;
	
	override function create() {
		Group {
			content: [
				Rectangle {
					fill: bind fill
					width: bind width
					height: 10
				}, Rectangle {
					fill: Color.web("#DBDBDB")
					width: bind width
					height: bind Math.max( 0, height - 10 )
					y: 10
				}
			]
		}
	}
}
