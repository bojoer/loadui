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
*Droppable.fx
*
*Created on mar 10, 2010, 11:13:10 fm
*/

package com.eviware.loadui.fx.ui.dnd;

import com.eviware.loadui.fx.ui.node.BaseMixin;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.util.Sequences;

import com.eviware.loadui.fx.FxUtils.*;

import java.util.Comparator;

/**
 * The Droppable which is currently being dragged over, if any.
 */
public-read var currentDroppable: Droppable = null;

def descendantOrdering = new DescendandComparator();

var currentDroppables:Droppable[] on replace {
	currentDroppable = if( sizeof currentDroppables == 0 ) null else currentDroppables[0];
}

/**
 * Mixin class for making BaseNodes droppable.
 *
 * @author dain.nilsson
 */
public mixin class Droppable extends BaseMixin {

	init {
		node.addMouseHandler( MOUSE_ENTERED, function( e:MouseEvent ):Void {
			def newDroppables = for( droppable in [ currentDroppables, this ][d|(d as Node).scene == (this as Node).scene] ) droppable;
			currentDroppables = Sequences.sort( newDroppables, descendantOrdering ) as Droppable[];
			if( isAcceptable( Draggable.currentDraggable ) ) {
				hovering = true;
			}
		} );
		
		node.addMouseHandler( MOUSE_EXITED, function( e:MouseEvent ):Void {
			delete this from currentDroppables;
			hovering = false;
		} );
	}
	
	/**
	 * Indicates if an acceptable Droppable is currently hovering over this Droppable.
	 */
	public-read var hovering = false;
	
	/**
	 * Defines which Draggable elements are able to be dropped onto this Droppable.
	 */
	public var accept: function( draggable:Draggable ):Boolean = function( draggable:Draggable ) { true };
	
	/**
	 * Action to be performed when an acceptable Draggable is dropped onto this Droppable.
	 */
	public var onDrop: function( draggable:Draggable ):Void;
	
	package function isAcceptable( draggable:Draggable ) {
		def dNode = draggable as BaseNode;
		
		draggable != null and
			not isDescendant( node, dNode ) and
			not isDescendant( dNode, node ) and
			accept( draggable )
	}
	
	package function drop( draggable:Draggable ) {
		hovering = false;
		onDrop( draggable );
	}
}

// TODO: Currently this orders based on if one Node is a descendent of another, 
// but this should probably be extended to order by z-index even if the two Nodes Parents aren't the same.
class DescendandComparator extends Comparator {
	override function compare( a:Object, b:Object ):Integer {
		if( isDescendant( a as Node, b as Node ) ) -1
		else if( isDescendant( b as Node, a as Node ) ) 1
		else 0;
	}
}