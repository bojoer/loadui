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
*Kitt.fx
*
*Created on apr 27, 2010, 10:30:00 fm
*/

package com.eviware.loadui.fx.ui;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Panel;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Math;

public def RUNNING = 0;
public def PAUSED = 1;
public def STOPPED = 2;

import javafx.animation.transition.*;
import javafx.lang.Duration;

public class Kitt extends CustomNode, Resizable {
override var styleClass = "kitt";

	public var backgroundFill:Paint = Color.web("#000000");
	public var ledFill:Paint = bind if(state == STOPPED) Color.web("#000000") else Color.web("#f20008");
	
	public var state = RUNNING on replace old {
		if( state == RUNNING ) {
			transTransition.play();
		} else if( state == PAUSED ) {
			transTransition.pause();
		} else if ( state == STOPPED ) {
			transTransition.stop();
		}
	}
	
	public-init var ledCount:Integer = 5;
	
	var opacityConsts:Number[] = [.2, .6, 1, .6, .2];
	 
	var position = 0.0;
	
	var ledGroup:Group;
	
	var transTransition:TranslateTransition;
    
	override function create() {
		def stepSize = width/(ledCount*2);
		var x = 0.0;
		def leds:Node[] = for( i in [0..<ledCount] ) {
			Rectangle {
				y: 1
				width: 3
				height: 3
				fill: bind ledFill
				opacity: opacityConsts[i]
			}
		}	

		for( node in leds ) {
			x += stepSize;
			node.layoutX = x + node.layoutBounds.minX - node.layoutBounds.width/2;
		}
		
		ledGroup = Group {
		    content: leds
		}
		
		transTransition = TranslateTransition {
		        duration: 1s node: ledGroup
		        fromX:0  byX: 60 toX: width - ledGroup.layoutBounds.width - 10
		        repeatCount:Number.POSITIVE_INFINITY autoReverse: true
		        framerate: 8
		    }
		    
		
		Group {
			content: [
				Rectangle {
					width: bind width
					height: bind height
					fill: bind backgroundFill
				}, ledGroup
				
			]
		}
	}
	
	override function getPrefHeight( width:Float ) { height }
	
	override function getPrefWidth( height:Float ) { width }
}