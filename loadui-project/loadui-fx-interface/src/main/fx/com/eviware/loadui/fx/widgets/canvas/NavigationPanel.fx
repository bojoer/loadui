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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.eviware.loadui.fx.util.ImageUtil.*;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.dnd.MovableNode;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.lang.Duration;
import javafx.animation.Interpolator;

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
	
	def objects = bind [ canvas.components, canvas.notes ] on replace {
		FX.deferAction( function():Void { createHolders(); shouldRefreshMinis = true; } );
	}
	
	def miniatures = Group {
		content: [ImageView { image: bind miniature, x: 0, y: 0 }]};
	
	var holders: Holder[] = [];
	function createHolders(): Void {
		delete holders;
		for( object in objects ) {
			insert 
				Holder {
					x: bind object.layoutX
					y: bind object.layoutY
					w: bind object.layoutBounds.width
					h: bind object.layoutBounds.height
					onChange: function(){
						FX.deferAction( function():Void { shouldRefreshMinis = true; } );
					}
				}
			into holders;
		}
	}
	
	postinit {
		timeline.playFromStart();
	}
	
	var timeline: Timeline = Timeline {
        repeatCount: Timeline.INDEFINITE
        keyFrames: [
	        KeyFrame {
	            time: 300ms
	            action: function() {
	            	if(shouldRefreshMinis){
	            		FX.deferAction( function():Void { refreshMinis(); shouldRefreshMinis = false; } );
	            	}
	            }
	            canSkip: true
	        }
        ]
   }
   
   // move to mouse pointer on click animation
   // current animated X position
   var animateX: Number on replace{
       handle.layoutX = animateX;
       updateCanvasPosition();
   }
   // current animated Y position
   var animateY: Number on replace{
       handle.layoutY = animateY;
       updateCanvasPosition();
   }
   // animation start and end positions, calculated on onMouseClick 
   var animateXStart: Number;
   var animateYStart: Number;
   var animateXEnd: Number;
   var animateYEnd: Number;
   
   // move animation timeline started on onMouseClick
	var moveAnimationTimeline: Timeline = Timeline {
	    keyFrames:[
	        KeyFrame { time: 0s values: [animateX => animateXStart, animateY => animateYStart] },
	        KeyFrame { time: 200ms values: [animateX => animateXEnd tween Interpolator.LINEAR, animateY => animateYEnd tween Interpolator.LINEAR] },
	    ]
	    repeatCount: 1
	}
	
	var shouldRefreshMinis: Boolean = false;
	
	var miniature: Image;
	function refreshMinis(): Void {
		miniature = canvas.createMiniatures(width - 32, height - 58);
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
									onMouseClicked: function( e:MouseEvent ) {
										if( e.button == MouseButton.PRIMARY ){
											animateXStart = handle.layoutX;
											animateXEnd = Math.max( 0, Math.min( map.width - handle.layoutBounds.width, e.x - handle.layoutBounds.width/2) );
											animateYStart = handle.layoutY;
											animateYEnd = Math.max( 0, Math.min( map.height - handle.layoutBounds.height, e.y - handle.layoutBounds.height/2) );
											moveAnimationTimeline.evaluateKeyValues();
											moveAnimationTimeline.playFromStart();
										}
									}
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
										updateCanvasPosition();
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
	
	function updateCanvasPosition() {
		canvas.offsetX = ( handle.layoutX + handle.translateX ) / scale as Integer;
		canvas.offsetY = ( handle.layoutY + handle.translateY ) / scale as Integer;
		canvas.refreshTerminals();
	}
	
	override function getPrefWidth( height:Float ) {
		width
	}
	
	override function getPrefHeight( width:Float ) {
		height
	}
}

class Holder {
	public var x on replace {
		onChange();
	}
	public var y on replace {
		onChange();
	}
	public var w on replace oldW {
		if(Math.abs(oldW - w) > 10){
			onChange();
		}
	}
	public var h on replace oldH {
		if(Math.abs(oldH - h) > 10){
			onChange();
		}
	}
	public var onChange: function();
}

