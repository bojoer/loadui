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
*DisplayContainer.fx
*
*Created on apr 19, 2010, 12:41:02 em
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Insets;
import javafx.util.Math;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.resources.ResizablePath;
import com.eviware.loadui.fx.ui.layout.LayoutContainerNode;

def gridImage = Image { url:"{__ROOT__}images/displayGrid.png" };

public class DisplayContainer extends LayoutContainerNode {

	def fill:Paint = Color.web("#000000");
	def stroke:Paint = Color.web("#909090");
	def glareFill:Paint = LinearGradient {
		endX: 0
		stops: [
			Stop { offset: 0, color: Color.rgb( 0xff, 0xff, 0xff, 0.4 ) },
			Stop { offset: 1, color: Color.rgb( 0xff, 0xff, 0xff, 0.0 ) }
		]
	}
	
	override var styleClass = "display-container";

	override function create() {
		Stack {
			content: [
				Rectangle {
					width: bind width
					height: bind height
					fill: fill
					stroke: stroke
				}, ImageView {
					image: gridImage
					viewport: bind Rectangle2D {
						width: Math.max( width - 4, 0 )
						height: Math.max( height - 4, 0 )
					}
				}, super.create(),
				Glare {
					width: bind width
					height: bind height
					fill: glareFill
					stroke: null
				}
			]
		}
	}
}

class Glare extends ResizablePath {
	override function calculatePath() {
		[
			MoveTo { x: 5, y: 2 },
			ArcTo { x: 2, y: 5, radiusX: 3, radiusY: 3 },
			LineTo { x: 2, y: height - 10 },
			QuadCurveTo { x: width - 3, y: 2, controlX: 5, controlY: 10 },
			ClosePath {}
		];
	}
}
