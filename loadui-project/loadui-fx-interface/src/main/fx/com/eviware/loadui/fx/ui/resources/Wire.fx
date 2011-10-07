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
*Wire.fx
*
*Created on feb 25, 2010, 14:09:45 em
*/

package com.eviware.loadui.fx.ui.resources;

import javafx.scene.Group;
import javafx.scene.CustomNode;
import javafx.scene.shape.CubicCurve;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.util.Math;

/**
 * A wire drawn from (startX, startY) to (endX, endY).
 */
public class Wire extends CustomNode {
	public var startX:Number;
	public var startY:Number;
	public var endX:Number;
	public var endY:Number;
	
	public var fill:Paint = Color.GRAY;
	
	def control = bind Math.min( Math.sqrt( Math.pow( startX - endX, 2) + Math.pow( startY - endY, 2) ), 200 );

	override function create() {
		Group {
			content: [
				CubicCurve {
					startX: bind startX
					startY: bind startY
					controlX1: bind startX
					controlY1: bind startY + control
					controlX2: bind endX
					controlY2: bind endY - control
					endX: bind endX
					endY: bind endY
		         fill: null
		         strokeWidth: 12
		         stroke: Color.TRANSPARENT
				}, CubicCurve {
					startX: bind startX
					startY: bind startY
					controlX1: bind startX
					controlY1: bind startY + control
					controlX2: bind endX
					controlY2: bind endY - control
					endX: bind endX
					endY: bind endY
		         fill: null
		         strokeWidth: 6
		         stroke: Color.BLACK
				}, CubicCurve {
					startX: bind startX
					startY: bind startY
					controlX1: bind startX
					controlY1: bind startY + control
					controlX2: bind endX
					controlY2: bind endY - control
					endX: bind endX
					endY: bind endY
		         fill: null
		         strokeWidth: 4
		         stroke: bind fill
				}
			]
		}
	}
}
