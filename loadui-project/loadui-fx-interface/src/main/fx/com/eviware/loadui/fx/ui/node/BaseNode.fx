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
*BaseNode.fx
*
*Created on mar 10, 2010, 09:31:53 fm
*/

package com.eviware.loadui.fx.ui.node;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;

public def MOUSE_CLICKED = 1;
public def MOUSE_PRESSED = 2;
public def MOUSE_RELEASED = 3;
public def MOUSE_DRAGGED = 4;
public def MOUSE_ENTERED = 5;
public def MOUSE_EXITED = 6;
public def MOUSE_MOVED = 7;
public def MOUSE_PRIMARY_CLICKED = 8;
public def MOUSE_WHEEL_MOVED = 9;

public def KEY_PRESSED = 11;
public def KEY_RELEASED = 12;

/**
 *	Replacement class for CustomNode which adds general functionality.
 *
 * @author dain.nilsson
 */
public class BaseNode extends CustomNode {
	def handlerMap = new HashMap();
	
	/**
	 * Adds a MouseEvent handler for the given mouse event.
	 */
	public function addMouseHandler( event:Integer, handler:function( e:MouseEvent ) ) {
		addHandler( event, handler );
	}
	
	/**
	 * Adds a KeyEvent handler for the given keyboard event.
	 */
	public function addKeyHandler( event:Integer, handler:function( e:KeyEvent ) ) {
		addHandler( event, handler );
	}
	
	/**
	 * Removes a MouseEvent handler for the given mouse event.
	 */
	public function removeMouseHandler( event:Integer, handler:function( e:MouseEvent ) ) {
		removeHandler( event, handler );
	}
	
	/**
	 * Removes a KeyEvent handler for the given keyboard event.
	 */
	public function removeKeyHandler( event:Integer, handler:function( e:KeyEvent ) ) {
		removeHandler( event, handler );
	}
	
	/**
	 * If instantiating BaseNode directly, set content as the Node content.
	 */
	public-init var contentNode:Node;
	
	override function create() { contentNode }
	
	override def onMouseClicked = function( e:MouseEvent ) {
		fireMouseEvent( MOUSE_CLICKED, e );
		if( e.button == MouseButton.PRIMARY )
			fireMouseEvent( MOUSE_PRIMARY_CLICKED, e );
	}
	override def onMousePressed = function( e:MouseEvent ) { fireMouseEvent( MOUSE_PRESSED, e ); }
	override def onMouseReleased = function( e:MouseEvent ) { fireMouseEvent( MOUSE_RELEASED, e ); }
	override def onMouseDragged = function( e:MouseEvent ) { fireMouseEvent( MOUSE_DRAGGED, e ); }
	override def onMouseEntered = function( e:MouseEvent ) { fireMouseEvent( MOUSE_ENTERED, e ); }
	override def onMouseExited = function( e:MouseEvent ) { fireMouseEvent( MOUSE_EXITED, e ); }
	override def onMouseMoved = function( e:MouseEvent ) { fireMouseEvent( MOUSE_MOVED, e ); }
	override def onMouseWheelMoved = function( e:MouseEvent ) { fireMouseEvent( MOUSE_WHEEL_MOVED, e ); }
	
	override def onKeyPressed = function( e:KeyEvent ) { fireKeyEvent( KEY_PRESSED, e ); }
	override def onKeyReleased = function( e:KeyEvent ) { fireKeyEvent( KEY_RELEASED, e ); }
	
	function removeHandler( event:Integer, handler:Object ):Void {
		def holder = (handlerMap.get( event ) as SeqHolder);
		if( holder != null )
			delete handler from holder.seq;
	}
	
	function addHandler( event:Integer, handler:Object ):Void {
		if( not handlerMap.containsKey( event ) )
			handlerMap.put( event, new SeqHolder() );
		
		insert handler into (handlerMap.get( event ) as SeqHolder).seq;
	}
	
	function fireMouseEvent( event:Integer, e:MouseEvent ) {
		def holder = (handlerMap.get( event ) as SeqHolder);
		if( holder != null )
			for( handler in holder.seq )
				(handler as function( e:MouseEvent ))( e );
	}
	
	function fireKeyEvent( event:Integer, e:KeyEvent ) {
		def holder = (handlerMap.get( event ) as SeqHolder);
		if( holder != null )
			for( handler in holder.seq )
				(handler as function( e:KeyEvent ))( e );
	}
}

class SeqHolder {
	var seq:Object[];
}
