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
*Canvas.fx
*
*Created on feb 24, 2010, 13:58:30 em
*/

package com.eviware.loadui.fx.widgets.canvas;

import javafx.util.Math;
import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.geometry.BoundingBox;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.widgets.ModelItemHolder;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.widgets.toolbar.ComponentToolbarItem;
import com.eviware.loadui.fx.widgets.canvas.TestCaseNode;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;

import java.util.EventObject;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.Canvas" );

/**
 * The work area of a CanvasItem. Displays contained ComponentItems and listens for changes.
 * 
 * @author dain.nilsson
 */
public class Canvas extends BaseNode, Droppable, ModelItemHolder, Resizable, EventHandler {
	override var layoutBounds = bind lazy BoundingBox { minX:0 minY:0 width:width height:height }
	
	protected def connectionLayer = Group {};
	protected def componentLayer = Group {};
	
	public def testcaseItem = modelItem as SceneItem;
	
	protected def layers = Group {
		content: [ connectionLayer, componentLayer ]
	}
	
	public var padding:Integer = 200;
	
	public var offsetX:Integer = 0 on replace {
		layers.layoutX = -offsetX;
	}
	public var offsetY:Integer = 0 on replace {
		layers.layoutY = -offsetY;
	}
	
	public-read var areaWidth:Integer = 0;
	public-read var areaHeight:Integer = 0;
	
	public-read var components:CanvasNode[] = bind componentLayer.content[c|c instanceof CanvasNode] as CanvasNode[];
	
	override var height on replace {
		refreshComponents();
	}
	
	override var width on replace {
		refreshComponents();
	}
	
	protected function acceptFunction( d:Draggable ) {
		d.node instanceof ComponentToolbarItem
	}
	override var accept = acceptFunction;
	
	protected function onDropFunction( d:Draggable ) {
		def sb = d.node.localToScene(d.node.layoutBounds);
		def x = sb.minX;
		def y = sb.minY;
		log.debug( "Component dropped at: (\{\}, \{\})", x, y );
		def component = createComponent( (d.node as ComponentToolbarItem).descriptor );
		component.setAttribute( "gui.layoutX", "{offsetX + x as Integer}" );
		component.setAttribute( "gui.layoutY", "{offsetY + y as Integer}" );
	}
	override var onDrop = onDropFunction;
	
	protected function createComponent( descriptor:ComponentDescriptor ):ComponentItem {
		var name = "{descriptor.getLabel()}";
		var i=0;
		while( sizeof canvasItem.getComponents()[c|c.getLabel() == name] > 0 )
			name = "{descriptor.getLabel()} ({++i})";

		log.debug( "Creating ComponentItem from descriptor: \{\} using label: \{\}", descriptor, name );
		
		canvasItem.createComponent( name, descriptor );
	}
	
	init {
		addMouseHandler( MOUSE_DRAGGED, onMouseDragged );
		addMouseHandler( MOUSE_PRESSED, onMouseDown );
		addMouseHandler( MOUSE_RELEASED, onMouseUp );
	}
	
	override function create() {
		Group {
			content: [
				Rectangle {
					fill: Color.TRANSPARENT
					width: bind width
					height: bind height
					onMouseClicked: function( e:MouseEvent ) {
						if( e.button == MouseButton.PRIMARY ) {
							Selectable.selectNone();
						}
					}
				}, layers,
				Rectangle {
					width: bind width
					height: bind height
					fill: Color.TRANSPARENT
					onMouseWheelMoved: function( e:MouseEvent ) {
						def newOffsetY = offsetY + 100 * e.wheelRotation as Integer;
						offsetY = Math.max( Math.min( newOffsetY, areaHeight-height as Integer), 0 );
					}
				}
			]
			clip: Rectangle { width: bind width, height: bind height }
		}
	}
	
	/**
	 * The CanvasItem to display.
	 */
	public var canvasItem:CanvasItem on replace oldItem {
		if( oldItem != null ) {
			oldItem.removeEventListener( BaseEvent.class, this );
			componentLayer.content = null;
			connectionLayer.content = null;
		}
		
		if( canvasItem != null ) {
			log.debug( "CanvasItem changed to: \{\}.", canvasItem );
			
			offsetX = 0;
			offsetY = 0;
			
			canvasItem.addEventListener( BaseEvent.class, this );
			for( component in canvasItem.getComponents() )
				addComponent( component );
			
			for( connection in canvasItem.getConnections() )
				addConnection(connection);
				
			refreshComponents();
		}
	}
	
	/**
	 * Refreshes all ConnectionNodes (calls terminalsChanged()).
	 */
	public function refreshTerminals():Void {
		for( node in connectionLayer.content ) {
			if( node instanceof ConnectionNode ) {
				(node as ConnectionNode).terminalsChanged();
			}
		}
	}
	
	/**
	 * Update the Canvas size when a component has been added/moved.
	 */
	public function refreshComponents():Void {
		if( sizeof componentLayer.content == 0 ) {
			areaWidth = width as Integer;
			areaHeight = height as Integer;
			return;
		}
		
		//log.debug( "Recalculating canvas area ( {areaWidth}, {areaHeight} )" );
		var minX = areaWidth;
		var minY = areaHeight;
		var maxX = 0;
		var maxY = 0;
		
		for( cmp in componentLayer.content ) {
			minX = Math.min( minX, cmp.layoutX as Integer );
			minY = Math.min( minY, cmp.layoutY as Integer );
			maxX = Math.max( maxX, cmp.layoutX + cmp.layoutBounds.width as Integer );
			maxY = Math.max( maxY, cmp.layoutY + cmp.layoutBounds.height as Integer );
		}
		
		def shiftX = if( padding - minX < 0 )
			Math.min( Math.max( padding - minX, (width as Integer) - ( maxX + padding ) ), 0 )
		else padding - minX;
		
		def shiftY = if( padding - minY < 0 )
			Math.min( Math.max( padding - minY, (height as Integer) - ( maxY + padding ) ), 0 )
		else padding - minY;
		
		for( cmp in componentLayer.content ) {
			cmp.layoutX += shiftX;
			cmp.layoutY += shiftY;
		}
		
		areaWidth = Math.max( width as Integer, maxX + shiftX + padding );
		areaHeight = Math.max( height as Integer, maxY + shiftY + padding );
		
		offsetX = Math.max( 0, Math.min( offsetX + shiftX, areaWidth - width as Integer ) );
		offsetY = Math.max( 0, Math.min( offsetY + shiftY, areaHeight - height as Integer ) );
		
		//log.debug( "Done recalculating Canvas area! New width, height = (\{\}, \{\})", areaWidth, areaHeight );
	}
	
	/**
	 * Locates and returns the CanvasNode for the ModelItem with the given id.
	 */
	public function lookupCanvasNode( id:String ):CanvasNode {
		componentLayer.lookup( id ) as CanvasNode;
	}
	
	function addComponent( component:ComponentItem ):Void {
		log.debug( "Adding ComponentItem \{\}", component );
		//def cmp = ComponentNode { id: component.getId(), canvas: this, component: component };
		def cmp = ComponentNode.create( component, this );
		insert cmp into componentLayer.content;
	}
	
	protected function removeModelItem( modelItem:ModelItem ):Void {
		log.debug( "Removing ModelItem \{\}", modelItem );
		def node = lookupCanvasNode( modelItem.getId() );
		node.deselect();
		delete node from componentLayer.content;
	}
	
	protected function addConnection( connection:Connection ):Void {
		log.debug( "Adding Connection \{\}", connection );
		insert ConnectionNode { id: getConnectionId( connection ), canvas:this, connection:connection } into connectionLayer.content;
	}
	
	function removeConnection( connection:Connection ):Void {
		log.debug( "Removing Connection \{\}", connection );
		def node = connectionLayer.lookup( getConnectionId( connection ) ) as ConnectionNode;
		node.deselect();
		delete node from connectionLayer.content;
	}
	
	function getConnectionId( connection:Connection ):String {
		"CONNECTION_{connection.getOutputTerminal().getId()}:{connection.getInputTerminal().getId()}"
	}
	
	override var modelItem = bind lazy canvasItem;
	
	override function release() {
		canvasItem = null;
	}
	
	override function handleEvent( e:EventObject ) {
		if( e instanceof CollectionEvent ) {
			def event = e as CollectionEvent;
			if( CanvasItem.COMPONENTS.equals( event.getKey() ) ) {
				if( event.getEvent() == CollectionEvent.Event.ADDED ) {
					runInFxThread( function() { addComponent( event.getElement() as ComponentItem ); refreshComponents(); } );
				} else {
					runInFxThread( function() { removeModelItem( event.getElement() as ComponentItem ); refreshComponents(); } );
				}
			} else if( CanvasItem.CONNECTIONS.equals( event.getKey() ) ) {
				if( event.getEvent() == CollectionEvent.Event.ADDED ) {
					runInFxThread( function() { addConnection( event.getElement() as Connection ) } );
				} else {
					runInFxThread( function() { removeConnection( event.getElement() as Connection ) } );
				}
			}
		}
	}
	
	override function getPrefHeight( width:Float ) {
		layers.layoutBounds.height
	}
	
	override function getPrefWidth( width:Float ) {
		layers.layoutBounds.width
	}
	
	var cStartX:Number;
	var cStartY:Number;
	var cDragging = false;
	function onMouseDown(e:MouseEvent):Void {
		if( e.controlDown ) {
			cDragging = true;
			cursor = Cursor.MOVE;
			cStartX = e.sceneX;
			cStartY = e.sceneY;
		}
	}
	
	function onMouseUp(e:MouseEvent):Void {
		cDragging = false;
		cursor = Cursor.DEFAULT;
	}
	
	function onMouseDragged(e:MouseEvent):Void {
		if( cDragging ) {
			def dX = e.sceneX - cStartX as Integer;
			def dY = e.sceneY - cStartY as Integer;
	
			offsetX = Math.max( 0, Math.min( offsetX - dX, areaWidth - width as Integer ) );
			offsetY = Math.max( 0, Math.min( offsetY - dY, areaHeight - height as Integer ) );
	
			cStartX = e.sceneX;
			cStartY = e.sceneY;
		}
	}
}
