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

package com.eviware.loadui.fx.widgets.canvas;

import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Stack;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.PathElement;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.layout.LayoutComponentNode;
import com.eviware.loadui.fx.ui.resources.ResizablePath;
import com.eviware.loadui.fx.ui.dialogs.DefaultComponentSettingsPanel;
import com.eviware.loadui.fx.dialogs.CloneComponentDialog;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.component.categories.TriggerCategory;
import com.eviware.loadui.api.model.CanvasObjectItem;


public function create( component:ComponentItem, canvas:Canvas ):ComponentNode {
	if( TriggerCategory.CATEGORY.equalsIgnoreCase( component.getCategory() ) )
		TriggerComponentNode { component: component, canvas: canvas, id: component.getId() }
	else
		ComponentNode { component: component, canvas: canvas, id: component.getId() }
}

public def roundedFrameStroke:Paint = Color.web("#898989");

public def roundedFrameFill:Paint = LinearGradient {
	endX: 0
	stops: [
		Stop { offset: 0, color: Color.web("#DEDEDE") },
		Stop { offset: 0.4, color: Color.web("#C9C9C9") },
		Stop { offset: 1, color: Color.web("#B5B5B5") }
	]
};

/**
 * Node to be displayed in a Canvas, representing a ComponentItem.
 * It can be moved around the Canvas, and its position will be stored in the project file.
 * 
 * @author dain.nilsson
 */
public class ComponentNode extends CanvasObjectNode {
	def faceHolder = Stack {};
	
	protected var roundedFrame:Node = RoundedBorder {
		fill: roundedFrameFill
		stroke: roundedFrameStroke
		layoutInfo: LayoutInfo { height: 50, vfill: true, hfill: true }
	};
	
	var face:LayoutComponentNode on replace oldFace {
		oldFace.release();
		faceHolder.content = [ roundedFrame, face ];
		body.visible = face != null;
		body.managed = face != null;
	}
	
	override var compact on replace {
		rebuildFace();
	}
	
	/**
	 * The ComponentItem to display.
	 */
	public var component:ComponentItem on replace {
		canvasObject = component;
		if( component != null ) {
			colorStr = component.getBehavior().getColor();
			rebuildFace();
		}
	}
	
	override var onSettings = function():Void {
		DefaultComponentSettingsPanel { component: component }.show();
	}
	
	override var onClone = function():Void {
		CloneComponentDialog { canvasObject: canvasObject };
	}
	
	override function create():Node {
		def dialog = super.create();
		if (component.getCompactLayout() == null) {
		    compactToggle.visible = false;
		}
		body.content = faceHolder;
		
		dialog;
	}
	
	override function onReloaded():Void {
		rebuildFace();
	}
	
	override function release() {
		component = null;
		face = null;
	}
	
	function rebuildFace():Void {
		face = LayoutComponentNode.buildLayoutComponentNode( if( compact ) component.getCompactLayout() else component.getLayout() );
	}
}

class RoundedBorder extends ResizablePath {
	override function calculatePath() {
		[
			MoveTo { x: 7, y: 0 },
			ArcTo { x: 0, y: 7, radiusX: 7, radiusY: 7 },
			LineTo { x: 0, y: height - 7 },
			ArcTo { x: 7, y: height, radiusX: 7, radiusY: 7 },
			LineTo { x: width - 7, y: height },
			ArcTo { x: width, y: height - 7, radiusX: 7, radiusY: 7 },
			LineTo { x: width, y: 7 },
			ArcTo { x: width - 7, y: 0, radiusX: 7, radiusY: 7 },
			ClosePath {}
		]
	}
}
