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
*SliderNode.fx
*
*Created on apr 15, 2010, 13:31:40 em
*/

package com.eviware.loadui.fx.ui.dnd;

import javafx.scene.Group;
import javafx.scene.Cursor;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Resizable;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.geometry.BoundingBox;
import javafx.util.Math;

import com.eviware.loadui.fx.ui.node.BaseNode;


public class SliderNode extends BaseNode, Resizable {
	public var numOptions:Integer = 1;
	
	public var selectedIndex:Integer = 0 on replace {
		handle.layoutY = notchSize*selectedIndex;
	}
	
	def notchSize = bind if(numOptions > 1) (height - contentNode.layoutBounds.height)/(numOptions-1) else 0 on replace {
		handle.layoutY = notchSize*selectedIndex;
	}
	
	override var blocksMouse = true;
	
	init {
		addMouseHandler( MOUSE_PRIMARY_CLICKED, function( e:MouseEvent ):Void {
			def pos = if( e.y < 0 ) 0 else if( e.y >= height ) height -1 else e.y;
			selectedIndex = pos/(height/numOptions) as Integer;
		} );
	}
	
	var handle:SliderNodeHandle;
	override function create() {
		Group {
			content: [
				Rectangle {
					width: bind width
					height: bind height
					fill: Color.TRANSPARENT
				}, handle = SliderNodeHandle {
					contentNode: contentNode
					cursor: if(cursor != null) cursor else Cursor.DEFAULT
				}
			]
		}
	}
	
	override function getPrefWidth( height:Float ) {
		contentNode.layoutBounds.width
	}
	
	override function getPrefHeight( width:Float ) {
		contentNode.layoutBounds.height * numOptions
	}
}

public class SliderNodeHandle extends BaseNode, Movable {
	override var onGrab = function() {
		containment = localToScene( BoundingBox {
			minX: layoutBounds.minX - layoutX
			minY: layoutBounds.minY - layoutY
			height: height
			width: layoutBounds.width
		} );
	}
	
	override var onMove = function() {
		selectedIndex = Math.round(layoutY/notchSize);
		layoutY = notchSize*selectedIndex;
	}
}
