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
*ConnectionNode.fx
*
*Created on feb 25, 2010, 10:59:28 fm
*/

package com.eviware.loadui.fx.widgets.canvas;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import com.eviware.loadui.fx.ui.resources.Wire;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;
import com.eviware.loadui.fx.ui.node.Deletable;

import com.eviware.loadui.api.terminal.Connection;
import java.lang.RuntimeException;

def highlightedCableColor = Color.web("#00adee");

/**
 * Represents a Connection between Terminals in the Canvas.
 * Drawn as a wire, connecting an OutputTerminal to an InputTerminal. 
 *
 * @author dain.nilsson
 */
public class ConnectionNode extends Selectable, Deletable, BaseNode {
	/**
	 * The Canvas in which the ConnectionNode lives.
	 */
	public-init var canvas:Canvas;
	
	/**
	 * The Connection to represent.
	 */
	public-init var connection: Connection;
	
	var startNode:TerminalNode;
	var endNode:TerminalNode;
	
	var startX:Number;
	var startY:Number;
	var endX:Number;
	var endY:Number;
	
	override var blocksMouse = true;
	
	override var onMouseClicked = function( e:MouseEvent ) {
		if( e.button == MouseButton.PRIMARY ) {
			toFront();
			requestFocus();
			if( e.controlDown ) { if( selected ) deselect() else select() } else if( not selected ) selectOnly();
		}
	}
	
	override var confirmDelete = false;
	
	override function doDelete():Void {
		connection.disconnect();
	}
	
	init {
		if( not FX.isInitialized( connection ) )
			throw new RuntimeException( "connection is not initialized!" );
		def outTerminal = connection.getOutputTerminal();
		def inTerminal = connection.getInputTerminal();
		startNode = canvas.lookupCanvasNode( outTerminal.getTerminalHolder().getId() ).lookupTerminalNode( outTerminal.getId() );
		endNode = canvas.lookupCanvasNode( inTerminal.getTerminalHolder().getId() ).lookupTerminalNode( inTerminal.getId() );
		
		terminalsChanged();
	}
	
	/**
	 * This needs to be called whenever the terminals move around inside their Container.
	 */
	public function terminalsChanged():Void {
		def sceneBoundsStart = startNode.localToScene( startNode.layoutBounds );
		startX = (sceneBoundsStart.maxX + sceneBoundsStart.minX) / 2;
		startY = (sceneBoundsStart.maxY + sceneBoundsStart.minY) / 2;
		
		def sceneBoundsEnd = endNode.localToScene( endNode.layoutBounds );
		endX = (sceneBoundsEnd.maxX + sceneBoundsEnd.minX) / 2;
		endY = (sceneBoundsEnd.maxY + sceneBoundsEnd.minY) / 2;
	}
		
	override function create() {
		Wire {
			startX: bind startX
			startY: bind startY
			endX: bind endX
			endY: bind endY
			fill: bind if( selected ) highlightedCableColor else Color.GRAY
			effect: bind if( selected ) Selectable.effect else null
		}
	}
}
