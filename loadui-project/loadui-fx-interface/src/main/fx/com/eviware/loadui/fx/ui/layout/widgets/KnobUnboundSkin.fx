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
import com.eviware.loadui.fx.FxUtils;

public class KnobUnboundSkin extends BaseNode, TooltipHolder {
	public-init var knob:Knob;
	
	/** Fill of the outer circle of the Knob. (CSS property: outer-circle-fill) */
	public var outerCircleFill:Paint = LinearGradient {
		stops: [
			Stop { offset: 0, color: Color.web("#ffffff") },
			Stop { offset: 1, color: Color.web("#8f8f8f") }
		]
	}
	
	/** Fill of the inner circle of the Knob. (CSS property: inner-circle-fill) */
	public var innerCircleFill:Paint = LinearGradient {
		endY: 0
		stops: [
			Stop { offset: 0, color: Color.web("#a6a6a6") },
			Stop { offset: 0.75, color: Color.web("#dbdbdb") },
			Stop { offset: 1, color: Color.web("#ffffff") }
		]
	}
	
	/** Fill of the outline of the Knob. (CSS property: outline-fill) */
	public var outlineFill:Paint = Color.web("#808080");
	
	/** Fill of the mark on the Knob. (CSS property: mark-fill) */
	public var markFill:Paint = LinearGradient {
		endY: 0
		stops: [
			Stop { offset: 0, color: Color.web("#666666") },
			Stop { offset: 1, color: Color.web("#ffffff") }
		]
	}
	
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
				Circle {
					centerX: 17
					centerY: 32
					radius: 14
					fill: bind shadowFill
				}, HandleNode { }
			]
		}
	}
}

class HandleNode extends BaseNode, TooltipHolder {
	override var tooltip = bind lazy "{knob.label}: {knob.value}";
	override var blocksMouse = true;
	
	var x = bind lazy knob.textBox.focused on replace {
	   enableTooltip( not knob.textBox.focused );
	}
	
	override function create() {
		Group {
			content: [
				Circle {
					centerX: 15
					centerY: 30
					radius: 14
					fill: bind outlineFill
				}, Circle {
					centerX: 15
					centerY: 30
					radius: 13
					fill: bind outerCircleFill
				}, Circle {
					centerX: 15
					centerY: 30
					radius: 11
					fill: bind innerCircleFill
				} Circle {
					fill: bind markFill
					centerX: bind 15 + 6 * Math.cos( knob.angle )
					centerY: bind 30 + 6 * Math.sin( knob.angle )
					radius: 3
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
						    var tmpValue = startValue + ( e.sceneX - startX+ startY - e.sceneY ) * knob.stepping;
						    if ( tmpValue >= knob.min and tmpValue <= knob.max )
								knob.numberValue = tmpValue
							else if ( tmpValue <= knob.min )
								knob.numberValue = knob.min
							else if ( tmpValue >= knob.max )
								knob.numberValue = knob.max;
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
