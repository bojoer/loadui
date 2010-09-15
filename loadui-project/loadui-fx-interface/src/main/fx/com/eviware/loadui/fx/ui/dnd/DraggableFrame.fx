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
*DraggableFrame.fx
*
*Created on mar 10, 2010, 14:46:48 em
*/

package com.eviware.loadui.fx.ui.dnd;

import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

/**
 * A Node which holds a Draggable, and remains in its place when the Draggable is being dragged.
 *
 * @author dain.nilsson
 */
public class DraggableFrame extends CustomNode {
	def group = Group {};
	
	/**
	 * The Draggable to hold.
	 */
	public var draggable:Draggable on replace {
		group.content = [ placeholder, draggable.node ];
	}
	
	/**
	 * A node to display behind the Draggable, which remains fixed when the Draggable is dragged.
	 */
	public var placeholder:Node on replace {
		group.content = [ placeholder, draggable.node ];
	}
	
	override var layoutBounds = bind placeholder.layoutBounds;
	
	override function create() {
		if( not FX.isInitialized( placeholder ) ) {
			placeholder = Rectangle {
				width: bind draggable.node.layoutBounds.width
				height: bind draggable.node.layoutBounds.height
				visible: bind draggable.dragging
				opacity: 0.5
				fill: Color.LIGHTBLUE
			}
		}
		
		group
	}
	
	override function toString():String {
		draggable.toString()
	}
}
