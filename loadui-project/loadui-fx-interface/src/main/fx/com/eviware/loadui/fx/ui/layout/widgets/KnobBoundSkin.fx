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
*KnobBoundSkin.fx
*
*Created on mar 25, 2010, 14:46:08 em
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.control.Label;
import javafx.scene.control.TextBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.geometry.BoundingBox;
import javafx.util.Math;

import com.eviware.loadui.fx.ui.resources.RadialLines;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import com.eviware.loadui.fx.ui.layout.Widget;
import com.eviware.loadui.fx.StylesheetAware;
import com.eviware.loadui.fx.FxUtils;

public class KnobBoundSkin extends BaseNode, TooltipHolder {
	public-init var knob:Knob;
	
	/** Fill of the outer circle of the Knob. (CSS property: outer-circle-fill) */
	public var outerCircleFill:Paint = LinearGradient {
		stops: [
			Stop { offset: 0, color: Color.web("#607cb3") },
			Stop { offset: 1, color: Color.web("#17253d") }
		]
	}
	
	/** Fill of the inner circle of the Knob. (CSS property: inner-circle-fill) */
	public var innerCircleFill:Paint = Color.web("#304b7d");
	
	/** Fill of the outline of the Knob. (CSS property: outline-fill) */
	public var outlineFill:Paint = Color.web("#000000");
	
	/** Fill of the mark on the Knob. (CSS property: mark-fill) */
	public var markFill:Paint = Color.web("#ffffff");
	
	/** Fill of the ticks surrounding the Knob. (CSS property: ticks-fill) */
	public var ticksFill:Paint = Color.web("#999999");
	
	/** Fill of the shadow of the Knob. (CSS property: shadow-fill) */
	public var shadowFill:Paint = RadialGradient {
		centerX: 0.5
		centerY: 0.5
		focusX: 0.5
		focusY: 0.5
		stops: [
			Stop { offset: 0.3, color: Color.web("#666666") },
			Stop { offset: 0.5, color: Color.TRANSPARENT }
		]
	};
	
	
	var startX:Number;
	var startY:Number;
	var startValue:Number;
	var dragging = false;
	
	override function create() {
		Group {
			content: [
				RadialLines {
					centerX: 15
					centerY: 30
					innerRadius: 12
					outerRadius: 14
					lines: 13
					angle: Math.PI / 8
					startAngle: 2 * Math.PI * 0.375
					fill: bind ticksFill
				}, Circle {
					centerX: 19
					centerY: 34
					radius: 11
					fill: bind shadowFill
				}, HandleNode { }
			]
		}
	}
}

class HandleNode extends BaseNode, TooltipHolder {
	override var tooltip = bind lazy "{knob.label}: {knob.value}";
	override var blocksMouse = true;
	
	override function create() {
		Group {
			content: [
				Circle {
					centerX: 15
					centerY: 30
					radius: 10
					fill: bind outlineFill
				}, Circle {
					centerX: 15
					centerY: 30
					radius: 9
					fill: bind outerCircleFill
				}, Circle {
					centerX: 15
					centerY: 30
					radius: 7.5
					fill: bind innerCircleFill
				} Line {
					stroke: bind markFill
					startX: 15
					startY: 30
					endX: bind 15 + 7.5 * Math.cos( knob.angle )
					endY: bind 30 + 7.5 * Math.sin( knob.angle )
				}
			]
		}
	}
	
	init {
		addMouseHandler( MOUSE_PRESSED, function( e:MouseEvent ):Void {
			if( e.button == MouseButton.PRIMARY ) {
				startX = e.sceneX;
				startY = e.sceneY;
				startValue = knob.numberValue;
				knob.textBox.opacity = 1;
				dragging = true;
			}
		} );
		
		addMouseHandler( MOUSE_DRAGGED, function( e:MouseEvent ):Void {
			if( dragging ) {
				knob.numberValue = startValue + ( e.sceneX - startX+ startY - e.sceneY ) * knob.stepping;
			}
		} );
		
		addMouseHandler( MOUSE_RELEASED, function( e:MouseEvent ):Void {
			if( dragging ) {
				if( e.clickCount > 1 ) {
					knob.textBox.requestFocus();
				} else {
					knob.textBox.opacity = 0;
				}
				dragging = false;
			}
		} );
	}
}
