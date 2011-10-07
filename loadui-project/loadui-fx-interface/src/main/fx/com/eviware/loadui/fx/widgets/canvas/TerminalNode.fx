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
*TerminalNode.fx
*
*Created on feb 25, 2010, 09:28:21 fm
*/

package com.eviware.loadui.fx.widgets.canvas;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Resizable;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.ArcTo;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.geometry.Insets;
import javafx.geometry.Bounds;

import com.eviware.loadui.fx.ui.popup.TooltipHolder;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.popup.Balloon;
import com.eviware.loadui.fx.ui.resources.Wire;

import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.util.StringUtils;

var wire:Wire;

var draggingTerminal:Terminal = null;

var inputAccept = false;
var outputAccept = false;

def white = RadialGradient {
	centerX: 0.5
	centerY: 0.5
	stops: [
		Stop { offset: 0, color: Color.rgb( 0xff, 0xff, 0xff, 0.8 ) },
		Stop { offset: 0.35, color: Color.rgb( 0xff, 0xff, 0xff, 0 ) }
	]
}

def green = RadialGradient {
	centerX: 0.5
	centerY: 0.5
	stops: [
		Stop { offset: 0, color: Color.rgb( 0xcc, 0xff, 0x0 ) },
		Stop { offset: 0.5, color: Color.rgb( 0xcc, 0xff, 0x0, 0 ) }
	]
}

/*def like = RadialGradient {
	centerX: 0.5
	centerY: 0.5
	stops: [
		Stop { offset: 0, color: Color.rgb( 0x0, 0xff, 0x0 ) },
		Stop { offset: 0.5, color: Color.rgb( 0x0, 0xff, 0x0, 0 ) }
	]
}*/

def like = RadialGradient {
		centerX: 0.5
		centerY: 0.5
		stops: [
			Stop { offset: 0, color: Color.rgb( 0xc1, 0xff, 0x06 ) },
			Stop { offset: 0.5, color: Color.rgb( 0xc1, 0xff, 0x06, 0 ) }
		]
	}

def sphere = RadialGradient {
	centerX: 0.4
	centerY: 0.4
	stops: [
		Stop { offset: 0, color: Color.rgb( 0xdb, 0xdc, 0xdd ) },
		Stop { offset: 1, color: Color.rgb( 0x50, 0x50, 0x50 ) }
	]
}

/**
 * A Node representing a Terminal on a Component.
 * TerminalNodes can be dragged and dropped onto other TermonalNodes causing a connection to be made,
 * as long as one holds an OutputTerminal and the other an InputTerminal. 
 *
 * @author dain.nilsson
 */
public class TerminalNode extends BaseNode, Resizable, Droppable {
	/**
	 * The Terminal to represent.
	 */
	public-init var terminal:Terminal;
	
	/**
	 * The Canvas that contains the TerminalNode.
	 */
	public-init var canvas:Canvas;
	public-init var canvasObjectNode:CanvasObjectNode;
	
	public var fill:Color = Color.GRAY;
	
	public var isDragging = false;
	
	override var layoutInfo = LayoutInfo { hfill: true, hgrow: Priority.SOMETIMES };

	override function create() {
		def flip = terminal instanceof OutputTerminal;
		
		addMouseHandler(
			MOUSE_ENTERED,
			function( e:MouseEvent ):Void {
				if( not canvas.wireIsBeingDragged ) {
					if( terminal instanceof InputTerminal ) {
						canvasObjectNode.showInputBalloons( this );
					} else {
						canvasObjectNode.showOutputBalloons( this );
					}
				}
			}
		);
	
		addMouseHandler(
			MOUSE_EXITED,
			function( e:MouseEvent ):Void {
				if( not isDragging and not canvas.wireIsBeingDragged ) {
					if( terminal instanceof InputTerminal ) {
						canvasObjectNode.hideInputBalloons();
					} else {
						canvasObjectNode.hideOutputBalloons();
					}
				}
			}
		);
		
		Stack {
			content: [
				Circle {
	 				centerY: 12
					radius: 24
					fill: Color.TRANSPARENT
				}, Circle {
					radius: 12
					fill: LinearGradient {
						endY: 0
						stops: [
							Stop { offset: 0, color: Color.rgb( 0xff, 0xff, 0xff, 0.3 ) },
							Stop { offset: 1, color: Color.rgb( 0x0, 0x0, 0x0, 0.3 ) }
						]
					}
					clip: Rectangle {
						y: if( flip ) -12 else 0
						x: -12
						width: 24
						height: if( flip ) 14 else 12
					}
				}, Circle {
					radius: 12
					fill: Color.BLACK
					clip: Rectangle {
						x: -12
						y: if( flip ) 5 else -12
						width: 24
						height: 7
					}
				}, Circle {
					radius: 30
					fill: bind if( draggingTerminal != null ) {
						if( ( not flip and inputAccept ) or ( flip and outputAccept ) ) {
							if( (terminal.getTerminalHolder() != draggingTerminal.getTerminalHolder()) and
									((terminal instanceof InputTerminal and draggingTerminal instanceof OutputTerminal 
									and (terminal as InputTerminal).likes( draggingTerminal as OutputTerminal )) or
									(terminal instanceof OutputTerminal and draggingTerminal instanceof InputTerminal 
									and (draggingTerminal as InputTerminal).likes( terminal as OutputTerminal )))
							) like else if( hover ) white else Color.TRANSPARENT;
						} else Color.TRANSPARENT;
					} else if( hover ) white else Color.TRANSPARENT
				}, Circle {
					radius: 10
					fill: bind if( flip ) Color.rgb( 0xc9, 0xc9, 0xc9 ) else fill
				}, DraggableFrame {
					draggable: TerminalDraggable { tNode: this }
					placeholder: Stack {
						content: [
							Circle {
								radius: 15
								fill: Color.TRANSPARENT
							}, Circle {
								radius: 8
								fill: Color.rgb( 0x4a, 0x4a, 0x4a )
							}, Circle {
								radius: 7
								fill: sphere
							}
						]
					}
				}
			]
		}
	}
	
	override var accept = function( d:Draggable ):Boolean {
		if( d instanceof TerminalDraggable ) {
			def other = (d as TerminalDraggable).currentTerminal;
			
			return ( ( terminal instanceof InputTerminal and other instanceof OutputTerminal )
				or ( terminal instanceof OutputTerminal and other instanceof InputTerminal ) );
		}
		
		false
	}

	override var onDrop = function( d:Draggable ):Void {
		def other = (d as TerminalDraggable).currentTerminal;
		
		if( terminal instanceof OutputTerminal ) {
			terminal.getTerminalHolder().getCanvas().connect( terminal as OutputTerminal, other as InputTerminal );
		} else {
			other.getTerminalHolder().getCanvas().connect( other as OutputTerminal, terminal as InputTerminal );
		}
	}
	
	override function getPrefHeight( width:Number ) { 30 }
	
	override function getPrefWidth( height:Number ) { 30 }
}

class TerminalDraggable extends BaseNode, Draggable {
	public-init var tNode:TerminalNode;
	
	override function create() {
		Stack {
			content: [
				Circle {
					radius: 15
					fill: Color.TRANSPARENT
				}, Circle {
					radius: 8
					fill: Color.rgb( 0x4a, 0x4a, 0x4a )
				}, Circle {
					radius: 7
					fill: sphere
				}
			]
		}
	}
	
	override var revert = false;
	
	var prev:ConnectionNode;
	var currentTerminal:Terminal;
	
	override var onGrab = function():Void {
		isDragging = true;
		canvas.wireIsBeingDragged = true;
		
		var startNode:Node;
		var balloonsHasBeenShown:Boolean = false;
		
		// Special behaviour when moving an existing wire to a new terminal (wire has to be selected first).
		if( sizeof Selectable.selects == 1 and Selectable.selects[0] instanceof ConnectionNode ) {
			def conn = Selectable.selects[0] as ConnectionNode;
			if( terminal == conn.connection.getInputTerminal() or terminal == conn.connection.getOutputTerminal() ) {
				def other = if( terminal instanceof InputTerminal ) conn.connection.getOutputTerminal()
					else conn.connection.getInputTerminal();
				
				var canvasNode: CanvasObjectNode = canvas.lookupCanvasNode(other.getTerminalHolder().getId());
				startNode = canvasNode.lookupTerminalNode( other.getId() );
				currentTerminal = (startNode as TerminalNode).terminal;
				prev = conn;
				prev.visible = false;
				
				if( terminal instanceof InputTerminal )
				{
					//canvasObjectNode.hideAllInputBalloonsButThis( tNode );
					canvas.showInputBalloons( canvasObjectNode );
					inputAccept = true;
				}
				else
				{
					//canvasObjectNode.hideAllOutputBalloonsButThis( tNode );
					canvas.showOutputBalloons( canvasObjectNode );
					outputAccept = true;
				}
				balloonsHasBeenShown = true
				
			}
		} else {
			Selectable.selectNone();
		}
		
		if( not balloonsHasBeenShown )
		{
			if( terminal instanceof InputTerminal )
			{
				canvasObjectNode.hideAllInputBalloonsButThis( tNode );
				canvas.showOutputBalloons( canvasObjectNode );
				outputAccept = true;
			}
			else
			{
				canvasObjectNode.hideAllOutputBalloonsButThis( tNode );
				canvas.showInputBalloons( canvasObjectNode );
				inputAccept = true;
			}
		}
		
		
		if( startNode == null ) {
			startNode = this;
			currentTerminal = terminal;
		}
		
		draggingTerminal = currentTerminal;
		
		def pw = startNode.layoutBounds.width / 2;
		def ph = startNode.layoutBounds.height / 2;
		def w = layoutBounds.width / 2;
		def h = layoutBounds.height / 2;
		
		wire = if( (terminal instanceof OutputTerminal and prev == null) or (terminal instanceof InputTerminal and prev != null) ) Wire {
			startX: bind startNode.localToScene( startNode.layoutBounds ).minX + pw
			startY: bind startNode.localToScene( startNode.layoutBounds ).minY + ph
			endX: bind localToScene( layoutBounds ).minX + translateX + w
			endY: bind localToScene( layoutBounds ).minY + translateY + h
		} else Wire {
			endX: bind startNode.localToScene( startNode.layoutBounds ).minX + pw
			endY: bind startNode.localToScene( startNode.layoutBounds ).minY + ph
			startX: bind localToScene( layoutBounds ).minX + translateX + w
			startY: bind localToScene( layoutBounds ).minY + translateY + h
		}
		
		insert wire into AppState.byScene( scene ).overlay.content;
		wire.toBack();
	}
	
	override var onRelease = function():Void {
		isDragging = false;
		canvas.wireIsBeingDragged = false;
		draggingTerminal = null;
		
		if( terminal instanceof InputTerminal )
		{
			//canvasObjectNode.hideInputBalloons();
			canvas.hideInputBalloons();
			canvas.hideOutputBalloons();
		}
		else
		{
			//canvasObjectNode.hideOutputBalloons();
			canvas.hideInputBalloons();
			canvas.hideOutputBalloons();
		}
		
		delete wire from AppState.byScene( scene ).overlay.content;
		
		inputAccept = false;
		outputAccept = false;
		
		if( prev != null ) {
			prev.visible = true;
			if( not tNode.hover )
				prev.connection.disconnect();
			prev = null;
		}
	}
}
