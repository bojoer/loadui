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
package com.eviware.loadui.fx.widgets;

import javafx.scene.Group;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.geometry.Insets;
import javafx.util.Math;

import com.eviware.loadui.fx.ui.resources.PlayShape;

public class ExecutionGraphic extends Stack {
	
	public var running = false on replace {
		if( running ) {
			tick();
			animation.playFromStart();
		} else {
			animation.stop();
		}
	}
	
	def animation = Timeline {
		repeatCount: Timeline.INDEFINITE,
		keyFrames: KeyFrame { time: 125ms, canSkip: true, action: tick }
	}
	
	def playShape = Group {
		layoutInfo: LayoutInfo { margin: Insets { left: 3 } }
		visible: bind not running
		content: [
			PlayShape {
				layoutY: 1
				width: 8
				height: 10
				fill: Color.web("#b3b4b5")
			}, PlayShape {
				width: 8
				height: 10
				fill: Color.web("#6d6e71")
			}
		]
	}
	
	def stopShape = Group {
		visible: bind running
		content: [
			Rectangle {
				width: 10
				height: 10
				fill: Color.web("#5e5f60")
			}, Rectangle {
				layoutX: 1
				layoutY: 1
				width: 10
				height: 10
				fill: Color.web("#c3c5c7")
			}
		]
	}
	
	def circles = for( color in [ Color.web("#f20017"), Color.web("#c10010"), Color.web("#910009"), Color.web("#600004"), Color.web("#300001") ] ) Circle { fill: color, radius: 2 }
	
	init {
		children = [
			Group {
				visible: bind running
				content: [
					Rectangle { width: bind width, height: bind height, fill: Color.TRANSPARENT },
					circles
				]
			}, playShape,
			stopShape
		];
	}
	
	var position = 0;
	function tick():Void {
		def nextPosition = position+1;
		for( circle in circles ) {
			positionCircle( circle, position-- )
		}
		position = nextPosition;
	}
	
	function positionCircle( circle:Circle, pos:Integer ) {
		circle.centerX = width/2 + Math.cos( Math.PI*pos/8 )*( width/2 - 2 );
		circle.centerY = height/2 + Math.sin( Math.PI*pos/8 )*( width/2 - 2 );
	}
}