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
*TriggerComponentNode.fx
*
*Created on apr 14, 2010, 16:28:12 em
*/

package com.eviware.loadui.fx.widgets.canvas;

import javafx.scene.Group;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.ArcTo;

import com.eviware.loadui.fx.ui.layout.widgets.OnOffSwitch;

import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.component.categories.TriggerCategory;

import java.util.EventObject;

public class TriggerComponentNode extends ComponentNode {
	
	override var roundedFrame = TriggerFrame {
		layoutX: 15
		layoutY: 20
		width: bind width - 30
		height: bind height - 50
		stroke: bind roundedFrameStroke
		fill: bind roundedFrameFill
	}
	
	var stateProperty:Property;
	var onState:Boolean on replace oldVal {
		stateProperty.setValue( onState );
	}

	init {
		insert [
			OnOffSwitch {
				layoutX: 5
				layoutY: 6
				state: bind onState with inverse
				managed: false
			}, Rectangle {
				width: 40
				height: 20
				fill: Color.TRANSPARENT
			}, Line {
				endY: 10
				stroke: bind separatorStroke
			}
		] before toolbarItemsLeft[0];
		stateProperty = (component.getBehavior() as TriggerCategory).getStateProperty();
		onState = stateProperty.getValue() as Boolean;
	}
	
	override function handleEvent( e:EventObject ) {
		super.handleEvent( e );
		if( e instanceof PropertyEvent ) {
			def event = e as PropertyEvent;
			if( event.getEvent() == PropertyEvent.Event.VALUE and event.getProperty() == stateProperty ) {
				onState = stateProperty.getValue() as Boolean;
			}
		}
	}
}

class TriggerFrame extends Path {
	init {
		recalculateShape();
	}

	function recalculateShape() {
		elements = [
			MoveTo { y: 17 },
			LineTo { y: height - 7 },
			ArcTo { x: 7, y: height, radiusX: 7, radiusY: 7 },
			LineTo { x: width - 7, y: height },
			ArcTo { x: width, y: height - 7, radiusX: 7, radiusY: 7 },
			LineTo { x: width, y: 7 },
			ArcTo { x: width - 7, radiusX: 7, radiusY: 7 },
			LineTo { x: 50 },
			ArcTo { x: 45, y: 5, radiusX: 5, radiusY: 5 },
			ArcTo { x: 40, y: 10, radiusX: 5, radiusY: 5, sweepFlag: true },
			LineTo { x: 7, y: 10 },
			ArcTo { x: 0, y: 17, radiusX: 7, radiusY: 7 }
		];
	}

	public var width:Number on replace {
		recalculateShape();
	}

	public var height:Number on replace {
		recalculateShape();
	}
}
