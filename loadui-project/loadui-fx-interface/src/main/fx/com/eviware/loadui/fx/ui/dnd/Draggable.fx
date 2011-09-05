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
*Draggable.fx
*
*Created on mar 10, 2010, 10:11:27 fm
*/

package com.eviware.loadui.fx.ui.dnd;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.node.BaseMixin;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;

import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Group;
import javafx.scene.layout.Container;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.animation.transition.TranslateTransition;
import javafx.util.Math;

import java.awt.MouseInfo;


/**
 * The Draggable which is currentDraggablely being dragged, if any.
 */
public-read var currentDraggable: Draggable = null;

/**
 * Mixin class for making BaseNodes draggable.
 *
 * @author dain.nilsson
 */
public mixin class Draggable extends BaseMixin {
	def myNode = this as BaseNode;
	
	/**
	 * The Node to use as a drag handle, defaults to the Draggable itself.
	 */
	public var handle: BaseNode on replace oldHandle {
		if( oldHandle != null ) {
			oldHandle.removeMouseHandler( MOUSE_PRESSED, onPressed );
			oldHandle.removeMouseHandler( MOUSE_DRAGGED, onDragged );
			oldHandle.removeMouseHandler( MOUSE_RELEASED, onReleased );
		}
		
		if( handle != null ) {
			handle.addMouseHandler( MOUSE_PRESSED, onPressed );
			handle.addMouseHandler( MOUSE_DRAGGED, onDragged );
			handle.addMouseHandler( MOUSE_RELEASED, onReleased );
			
			if( handle.cursor == null )
				handle.cursor = hoverCursor;
		}
	}
	
	/**
	 * The pointer cursor that appears when hovering the Draggable.
	 */
	public var hoverCursor: Cursor = Cursor.MOVE;
	
	/**
	 * A Bounds for in which to contain the Draggable when dragging.
	 */
	public var containment: Bounds = null;
	
	/**
	 * Action to be performed whenever the Draggable is grabbed.
	 */
	public var onGrab: function():Void;
	
	/**
	 * Action to be performed whenever the Draggable is released.
	 */
	public var onRelease: function():Void;
	
	/**
	 * Action to be performed whenever the Draggable is being dragged.
	 */
	public var onDragging: function():Void;
	
	/**
	 * True if the Draggable is currentDraggablely being dragged.
	 */
	public-read var dragging = false;
	
	/**
	 * If set to true, the Draggable will revert to its initial position upon being dropped using an animation.
	 * If false, the Draggable will immediately be positioned back at the initial position, without animation.
	 */
	public var revert = true;
	
	/**
	 * Is set to false, do not use the overlay for items being dragged.
	 */
	public var useOverlay = true;
	
	/**
	 * The last MouseEvent to activate one of the onGrab, onRelease or onDragging functions.
	 */
	public-read protected var mouseEvent: MouseEvent;
	
	postinit {
		if( not FX.isInitialized( handle ) )
			handle = myNode;
	}
	
	def revertAnim = TranslateTransition {
		node: myNode
		toX: 0
		toY: 0
		action: function() {
			dragging = false;
		}
	}
	
	protected var startX = 0.0;
   protected var startY = 0.0;
   var startBounds:Bounds;
   var realParent:Parent;
   var initX = 0.0;
   var initY = 0.0;
   var myScene:Scene;
	
	function onPressed( e:MouseEvent ) {
		if( currentDraggable != null or not e.primaryButtonDown or revertAnim.running )
			return;
		
		def location = MouseInfo.getPointerInfo().getLocation();
		
		revertAnim.stop();
		currentDraggable = this;
		dragging = true;
		
		startX = location.x;
		startY = location.y;
		startBounds = myNode.localToScene( myNode.layoutBounds );
		realParent = myNode.parent;
		
		initX = myNode.layoutX;
		initY = myNode.layoutY;
		
		if( useOverlay ) {
			if( myScene == null )
				myScene = (this as Node).scene;
			if( realParent instanceof Group )
				delete myNode from (realParent as Group).content
			else if( realParent instanceof Container )
				delete myNode from (realParent as Container).content;
			node.layoutX = startBounds.minX - myNode.layoutBounds.minX;
			node.layoutY = startBounds.minY - myNode.layoutBounds.minY;
			insert myNode into AppState.byScene( myScene ).overlay.content;
		}
		
		mouseEvent = e;
		onGrab();
	}
	
	function onDragged( e:MouseEvent ) {
		if( not dragging )
			return;
		
		def location = MouseInfo.getPointerInfo().getLocation();
		
		var tx = location.x - startX;
		if( containment != null ) {
			tx = if( startBounds.minX + tx < containment.minX )
				containment.minX - startBounds.minX
			else if( startBounds.maxX + tx  > containment.maxX )
				containment.maxX - startBounds.maxX
			else
				tx;
		}
		
		var ty = location.y - startY;
		if( containment != null ) {
			ty = if( startBounds.minY + ty < containment.minY )
				containment.minY - startBounds.minY
			else if( startBounds.maxY + ty > containment.maxY )
				containment.maxY - startBounds.maxY
			else
				ty;
		}
		
		myNode.translateX = tx;
		myNode.translateY = ty;
		
		mouseEvent = e;
		onDragging();
	}
	
	protected function onReleased( e:MouseEvent ) {
		if( not dragging or revertAnim.running )
			return;
		
		def droppable = Droppable.currentDroppable;
		if( e.button == MouseButton.PRIMARY and droppable.isAcceptable( this ) )
			droppable.drop( this );
		
		if( useOverlay ) {
			delete myNode from AppState.byScene( myScene ).overlay.content;
			myNode.layoutX = initX;
			myNode.layoutY = initY;
			if( realParent instanceof Group )
				insert myNode into (realParent as Group).content
			else if( realParent instanceof Container )
				insert myNode into (realParent as Container).content;
		}
		
		mouseEvent = e;
		onRelease();
		
		currentDraggable = null;
		
		if( revert and ( Math.abs( myNode.translateX ) > 5 or Math.abs( myNode.translateY ) > 5 ) ) {
			revertAnim.playFromStart();
		} else {
			myNode.translateX = 0;
			myNode.translateY = 0;
			dragging = false;
		}
	}
}
