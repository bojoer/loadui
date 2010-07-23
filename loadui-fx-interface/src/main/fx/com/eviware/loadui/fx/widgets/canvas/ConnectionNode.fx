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
	
	var startComponentNode:CanvasNode;
	var endComponentNode:CanvasNode;
	var scnOffsetX:Number;
	var scnOffsetY:Number;
	var ecnOffsetX:Number;
	var ecnOffsetY:Number;
	
	var startNode:Node;
	var endNode:Node;
	
	def startX = bind startComponentNode.layoutX + startComponentNode.translateX + scnOffsetX;
	def startY = bind startComponentNode.layoutY + startComponentNode.translateY + scnOffsetY;
	def endX = bind endComponentNode.layoutX + endComponentNode.translateX + ecnOffsetX;
	def endY = bind endComponentNode.layoutY + endComponentNode.translateY + ecnOffsetY;
	
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
		
		terminalsChanged();
	}
	
	/**
	 * This needs to be called whenever the terminals move around inside their Container.
	 */
	public function terminalsChanged():Void {
		def inTerminal = connection.getInputTerminal();
		def outTerminal = connection.getOutputTerminal();
		
		startComponentNode = canvas.lookupCanvasNode( outTerminal.getTerminalHolder().getId() );
		if (startComponentNode instanceof ComponentNode) {
			startNode = (startComponentNode as ComponentNode).lookupTerminalNode( outTerminal.getId() );
			scnOffsetX = startNode.localToScene( startNode.layoutBounds ).minX
				- startComponentNode.localToScene( startComponentNode.layoutBounds ).minX + startNode.layoutBounds.width / 2 - 2;
			scnOffsetY = startNode.localToScene( startNode.layoutBounds ).minY
				- startComponentNode.localToScene( startComponentNode.layoutBounds ).minY + 20;//+ startNode.layoutBounds.height / 2;
		}
		
		endComponentNode = canvas.lookupCanvasNode( inTerminal.getTerminalHolder().getId() );
		if (endComponentNode instanceof ComponentNode) {
			endNode = (endComponentNode as ComponentNode).lookupTerminalNode( inTerminal.getId() );
		} else {
		    endNode = (endComponentNode as TestCaseNode).getStateTerminalNode();
		}
		ecnOffsetX = endNode.localToScene( endNode.layoutBounds ).minX
			- endComponentNode.localToScene( endComponentNode.layoutBounds ).minX + endNode.layoutBounds.width / 2 - 2;
		ecnOffsetY = endNode.localToScene( endNode.layoutBounds ).minY
			- endComponentNode.localToScene( endComponentNode.layoutBounds ).minY + 25;// + endNode.layoutBounds.height / 2;

	}
	
	//Terminal node in TestCaseNode changes layoutX position during TestCaseNode component creation (it is bounded
	//to width of RunController which obviously changes). This can cause TestCaseConmponent's connection to be displaced
	//a bit from its position. This is fix for that. It should occure only on component creation. 
	var endNodeLayoutX: Number = bind endNode.layoutX on replace {
	//	if (endComponentNode instanceof TestCaseNode) {
			terminalsChanged();
	//	}
	}
	
	var startNodeLayoutY:Number = bind startNode.layoutY on replace {
	    terminalsChanged();
	}
	
	var endNodeLayoutY:Number = bind endNode.layoutY on replace {
		    terminalsChanged();
		}
		
	var startNodeLayoutX:Number = bind startNode.layoutX on replace {
		    terminalsChanged();
		}
		
	override function create() {
		Wire {
			startX: bind startX
			startY: bind startY
			endX: bind endX
			endY: bind endY
			fill: Color.GRAY
			effect: bind if( selected ) Selectable.effect else null
		}
	}
}
