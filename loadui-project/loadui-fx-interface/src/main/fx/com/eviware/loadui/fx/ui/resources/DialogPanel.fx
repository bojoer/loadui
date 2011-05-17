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
*DialogPanel.fx
*
*Created on aug 10, 2010, 11:09:15 fm
*/

package com.eviware.loadui.fx.ui.resources;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;

import com.sun.javafx.scene.layout.Region;

public class DialogPanel extends Stack {
	def highlightRegion = Region {
		styleClass: "dialog-panel-highlight";
		layoutInfo: LayoutInfo {
			hfill: true
			vfill: true
		}
	};
	
	def titleRegion = Region {
		styleClass: "dialog-panel-title";
		layoutInfo: LayoutInfo {
			vpos: VPos.TOP
			hfill: true
			height: 30
			maxHeight: 30
			vgrow: Priority.NEVER
		}
	};
	
	public-read def titlebarNode:Node = titleRegion;
	
	def baseRegion = Region {
		styleClass: "dialog-panel";
		layoutInfo: LayoutInfo {
			hfill: true
			vfill: true
		}
	};
	
	def gloss = GlossShape {
		fill: Color.rgb( 0xff, 0xff, 0xff, 0.5 )
		stroke: null
		layoutInfo: LayoutInfo {
			hfill: true
			vfill: true
		}
	};
	
	def shadow = ShadowShape {
		fill: Color.rgb( 0, 0, 0, 0.3 )
		stroke: null
		layoutInfo: LayoutInfo {
			hfill: true
			vfill: true
		}
	};
	
	public var highlight = false on replace {
		highlightRegion.visible = highlight;
	}
	
	public var titlebarColor:String on replace {
		if( titlebarColor != null ) {
			titleRegion.style = "-fx-titlebar-color: {titlebarColor};";
		}
	}
	
	function refreshChildren() {
		children = [ highlightRegion, baseRegion, titleRegion, gloss, shadow, body ];
	}
	
	public var body:Node on replace {
		refreshChildren();
	}
	
	init {
		refreshChildren();
	}
}

class GlossShape extends ResizablePath {
	override function calculatePath() {
		[
			MoveTo { x: width - 7, y: 3 },
			LineTo { x: 10, y: 3 },
			ArcTo { x: 2, y: 11, radiusX: 8, radiusY: 8 },
			LineTo { x: 2, y: height - 8 },
			LineTo { x: 4, y: height - 8 },
			LineTo { x: 4, y: 10 },
			ArcTo { x: 9, y: 5, radiusX: 5, radiusY: 5, sweepFlag: true },
			LineTo { x: width - 7, y: 5 },
			ClosePath {}
		];
	}
}

class ShadowShape extends ResizablePath {
	override function calculatePath() {
		[
			MoveTo { x: 8, y: height - 2 },
			LineTo { x: width - 10, y: height - 2 },
			ArcTo { x: width - 2, y: height - 10, radiusX: 8, radiusY: 8 },
			LineTo { x: width - 2, y: 10 },
			LineTo { x: width - 4, y: 10 },
			LineTo { x: width - 4, y: height - 9 },
			ArcTo { x: width - 9, y: height - 4, radiusX: 5, radiusY: 5, sweepFlag: true },
			LineTo { x: 8, y: height - 4 },
			ClosePath {}
		];
	}
}