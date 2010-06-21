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
*RadialLines.fx
*
*Created on mar 22, 2010, 14:10:49 em
*/

package com.eviware.loadui.fx.ui.resources;

import javafx.scene.Group;
import javafx.scene.CustomNode;
import javafx.scene.shape.Line;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.util.Math;

public class RadialLines extends CustomNode {
	var initialized = false;
	
	public var centerX = 0.0 on replace {
		if(initialized) drawLines();
	}
	
	public var centerY = 0.0 on replace {
		if(initialized) drawLines();
	}
	
	public var innerRadius = 0.0 on replace {
		if(initialized) drawLines();
	}
	
	public var outerRadius = 1.0 on replace {
		if(initialized) drawLines();
	}
	
	public var lines = 5 on replace {
		if(initialized) drawLines();
	}
	
	public var fill:Paint = Color.BLACK on replace {
		if(initialized) drawLines();
	}
	
	public var angle:Number on replace {
		if(initialized) drawLines();
	}
	
	public var startAngle:Number = 0.0 on replace {
		if(initialized) drawLines();
	}
	
	def group = Group {};
	
	override function create() {
		initialized = true;
		drawLines();
		
		group
	}
	
	function drawLines() {
		if( lines < 1 )
			return;
		
		def angleStep = if( FX.isInitialized(angle) ) angle else 2*Math.PI / lines;
		group.content = for( n in [0..<lines] ) {
			Line {
				startX: getPointX( innerRadius, n*angleStep )
				startY: getPointY( innerRadius, n*angleStep )
				endX: getPointX( outerRadius, n*angleStep )
				endY: getPointY( outerRadius, n*angleStep )
				stroke: fill
			}
		}
	}
	
	function getPointX( radius:Number, angle:Number ) {
		centerX + radius * Math.cos( startAngle + angle )
	}
	
	function getPointY( radius:Number, angle:Number ) {
		centerY + radius * Math.sin( startAngle + angle )
	}
}
