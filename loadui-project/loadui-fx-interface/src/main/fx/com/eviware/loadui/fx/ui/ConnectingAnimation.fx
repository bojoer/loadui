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

package com.eviware.loadui.fx.ui;

import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.geometry.Insets;
import javafx.util.Math;

public class ConnectingAnimation extends Stack {
	override var styleClass = "connecting-animation";
	override var layoutInfo = LayoutInfo { width: 13, height: 11, margin: Insets { left: 3, right: 3 } }
	override var children = [
		SVGPath {
			content: "M 3 0 L 11 0 11 6 9 6 9 2 3 2"
			fill: Color.rgb( 0x66, 0x66, 0x66 )
		}, SVGPath {
			content: "M 0 3 L 8 3 8 9 0 9 z M 6 11 L 7 11 7 12 6 12 z M 8 11 L 9 11 9 12 8 12 z M 10 11 L 11 11 11 12 10 12 z M 12 11 L 13 11 13 12 12 12 z M 12 9 L 13 9 13 10 12 10 z M 12 7 L 13 7 13 8 12 8 z M 12 5 L 13 5 13 6 12 6 z"
			fill: Color.rgb( 0x80, 0x80, 0x80 )
		}
	];
	
	var dir = 1;
	var index = 0;
	
	def animation = Timeline {
		repeatCount: Timeline.INDEFINITE,
		keyFrames: KeyFrame { time: 500ms, action: positionLight }
	}
	
	def light = Rectangle {
		x: -1
		y: -1
		width: 3
		height: 3
		fill: Color.rgb( 0xf3, 0x86, 0x1b )
	}
	
	init {
		insert light into children;
		positionLight();
		animation.playFromStart();
	}
	
	function positionLight():Void {
		index += dir;
		if( index == 0 or index == 6 ) {
			dir *= -1;
		}
		
		light.layoutX = 6 + 2*Math.min( index, 3 );
		light.layoutY = 11 - 2*Math.max( index - 3, 0 );
	}
}