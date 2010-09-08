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
import javafx.scene.control.Separator;
import javafx.geometry.BoundingBox;

import com.javafx.preview.control.PopupMenu;
import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.Menu;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.widgets.ModelItemHolder;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.widgets.toolbar.ComponentToolbarItem;
import com.eviware.loadui.fx.widgets.canvas.TestCaseNode;
import com.eviware.loadui.fx.dialogs.CreateNewTestCaseDialog;
import com.eviware.loadui.fx.dialogs.CloneCanvasObjectsDialog;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.fx.util.ImageUtil.*;

import java.util.EventObject;
import java.util.HashMap;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Sequences;
import javafx.scene.image.Image;
import javafx.geometry.BoundingBox;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.Canvas" );

public var showNotes = true;

/**
 * The work area of a CanvasItem. Displays contained ComponentItems and listens for changes.
 * 
 * @author dain.nilsson
 */
public class Canvas extends BaseNode, Droppable, ModelItemHolder, Resizable, EventHandler { 
	override var layoutBounds = bind lazy BoundingBox { minX:0 minY:0 width:width height:height }
	
	def dummyNodeConnections = Rectangle { fill: Color.rgb(0,0,0,0.0001), width: 1, height: 1 };
	def dummyNodeComponents = Rectangle { fill: Color.rgb(0,0,0,0.0001), width: 1, height: 1 };
	def dummyNodeNotes = Rectangle { fill: Color.rgb(0,0,0,0.0001), width: 1, height: 1 };
	
	protected def connectionLayer = Group { content: dummyNodeConnections };
	protected def componentLayer = Group { content: dummyNodeComponents };
	protected def noteLayer = Group { visible: bind showNotes, content: dummyNodeNotes };
	
	public def testcaseItem = modelItem as SceneItem;
	
	protected def layers = Group {
		content: [ noteLayer, connectionLayer, componentLayer ]
	}
	
	def _showNotes = bind showNotes on replace {
		if( showNotes )
			setNoteLayer( true );
	}
	
	public function setNoteLayer( front:Boolean ) {
		layers.content = if( front )
			[ connectionLayer, componentLayer, noteLayer ]
		else
			[ noteLayer, connectionLayer, componentLayer ];
	}
	
	public var padding:Integer = 200;
	
	public var offsetX:Integer = 0 on replace {
		//componentLayer.layoutX = -offsetX;
		//noteLayer.layoutX = -offsetX;
		layers.layoutX = -offsetX;
		connectionLayer.layoutX = offsetX;
	}
	public var offsetY:Integer = 0 on replace {
		//componentLayer.layoutY = -offsetY;
		//noteLayer.layoutY = -offsetY;
		layers.layoutY = -offsetY;
		connectionLayer.layoutY = offsetY;
	}
	
	public-read var areaWidth:Integer = 0;
	public-read var areaHeight:Integer = 0;
	
	public-read var components:CanvasObjectNode[] = bind componentLayer.content[c|c instanceof CanvasObjectNode] as CanvasObjectNode[] on replace {
		FX.deferAction( function():Void { refreshComponents() } );
	}
	
	public-read var notes:Note[] = bind noteLayer.content[c|c instanceof Note] as Note[] on replace {
		FX.deferAction( function():Void { refreshComponents() } );
	}
	
	public function createMiniatures(maxWidth: Number, maxHeight: Number): Image {
		def scale = Math.min(maxWidth / areaWidth, maxHeight / areaHeight);
		
		var noteImg = nodeToImage(noteLayer, areaWidth, areaHeight);
		noteImg = scaleImage(noteImg, scale);
		var connImg = nodeToImage(connectionLayer, areaWidth, areaHeight);
		connImg = scaleImage(connImg, scale);
		var compImg = nodeToImage(componentLayer, areaWidth, areaHeight);
		compImg = scaleImage(compImg, scale);
		var img = combineImages(connImg, compImg);
		img = combineImages(noteImg, img);
		
		bufferedToFXImage(img);
	}
	
	public function createMiniatures(maxWidth: Number, maxHeight: Number, minScaleFactor: Number): String {
		def scale = Math.max(Math.min(maxWidth / areaWidth, maxHeight / areaHeight), minScaleFactor);

		var noteImg = nodeToImage(noteLayer, areaWidth, areaHeight);
		noteImg = scaleImage(noteImg, scale);
		var connImg = nodeToImage(connectionLayer, areaWidth, areaHeight);
		connImg = scaleImage(connImg, scale);
		var compImg = nodeToImage(componentLayer, areaWidth, areaHeight);
		compImg = scaleImage(compImg, scale);
		var img = combineImages(connImg, compImg);
		img = combineImages(noteImg, img);
		
		img = clipImage(img, (padding - 10) * scale, (padding - 10) * scale, (areaWidth - 2*padding + 20) * scale, (areaHeight - 2*padding + 20) * scale);
		img = clipImage(img, 0, 0, Math.min(maxWidth, img.getWidth()), Math.min(maxHeight, img.getHeight()));

		bufferedImageToBase64(img);
	}
	
	public function generateMiniatures() {
		canvasItem.setAttribute("miniature", createMiniatures(368, 188, 0.1));
	}
	
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
		AppState.instance.blockingTask( function():Void {
			def component = createComponent( (d.node as ComponentToolbarItem).descriptor );
			component.setAttribute( "gui.layoutX", "{offsetX + x as Integer}" );
			component.setAttribute( "gui.layoutY", "{offsetY + y as Integer}" );
		}, null, "Creating component." );
	}
	override var onDrop = onDropFunction;
	
	public function createNote():Void {
		def x = if( contextMouseEvent != null ) contextMouseEvent.sceneX else scene.width / 2;
		def y = if( contextMouseEvent != null ) contextMouseEvent.sceneY else scene.height / 2;
		contextMouseEvent = null;
		
		createNote( x, y );
	}
	
	public function createNote( x:Number, y:Number ):Void {		
		insert Note.createNote( this, x, y ) into noteLayer.content;
		showNotes = true;
	}
	
	public function removeNote( note:Note ):Void {
		delete note from noteLayer.content; 
	}
	
	public function createComponent( descriptor:ComponentDescriptor ):ComponentItem {
		var name = "{descriptor.getLabel()}";
		var i=0;
		while( sizeof canvasItem.getComponents()[c|c.getLabel() == name] > 0 )
			name = "{descriptor.getLabel()} ({++i})";

		log.debug( "Creating ComponentItem from descriptor: \{\} using label: \{\}", descriptor, name );
		
		canvasItem.createComponent( name, descriptor );
	}
	
	def deleteAction = MenuItem {
		text: "Delete"
		action: function():Void {
			Deletable.deleteObjects( for( deletable in Selectable.selects[s|s instanceof Deletable] ) deletable as Deletable, Selectable.selectNone );
		}
	}
	
	def cloneAction = MenuItem {
		text: "Clone"
		action: function():Void {
			for( clone in moveComponents( canvasItem, for( cNode in Selectable.selects[s|s instanceof CanvasObjectNode] ) (cNode as CanvasObjectNode).modelItem as CanvasObjectItem, false ) ) {
				def layoutX = Integer.parseInt( clone.getAttribute( "gui.layoutX", "0" ) ) + 50;
				def layoutY = Integer.parseInt( clone.getAttribute( "gui.layoutY", "0" ) ) + 50;
				clone.setAttribute( "gui.layoutX", "{ layoutX as Integer }" );
				clone.setAttribute( "gui.layoutY", "{ layoutY as Integer }" );
			}
		}
	}
	
	def addNoteAction = MenuItem {
		text: "Add note"
		action: function():Void {
			createNote();
		}
	}
	
	def moveSubmenu:Menu = Menu {
		text: "Clone/Move to"
		onShowing: function() {
			def selection = for( cn in Selectable.selects[s|s instanceof CanvasObjectNode] ) cn as CanvasObjectNode;
			def project = if( canvasItem instanceof ProjectItem ) canvasItem as ProjectItem else canvasItem.getProject();
			if( sizeof selection > 0 and sizeof selection[s|s instanceof TestCaseNode] == 0 ) {
				def components = for( cNode in selection ) cNode.modelItem as ComponentItem;
				moveSubmenu.items = [
					if( not ( canvasItem instanceof ProjectItem ) ) [ MenuItem {
						text: "Parent Project"
						action: function():Void {
							//moveComponents( project, components, true );
							CloneCanvasObjectsDialog { target: project, objects: components };
						}
					}, Separator {} ] else null,
					for( tc in project.getScenes()[t|t != canvasItem] ) MenuItem {
						text: tc.getLabel()
						action: function():Void {
							CloneCanvasObjectsDialog { target: tc, objects: components };
							//moveComponents( tc, components, true );
						}
					},
					Separator {},
					MenuItem {
						text: "New TestCase..."
						action: function():Void {
							CreateNewTestCaseDialog { project: project, onOk: function( testCase: SceneItem ):Void {
								CloneCanvasObjectsDialog { target: testCase, objects: components };
								//moveComponents( testCase, components, true );
							} }
						}
					}
				];
			} else {
				moveSubmenu.items = MenuItem {
					text: if( sizeof selection == 0 ) "Nothing selected" else "Cannot move selection containing TestCase"
					disable: true
				};
			}
		}
		onHidden: function() { //This shouldn't be needed, but is required for now due to a bug in the JavaFX PopupMenu.
			contextMenu.hide();
		}
	}
	
	function moveComponents( target:CanvasItem, objects:CanvasObjectItem[], deleteInitial:Boolean ):CanvasObjectItem[] {
		def clones = new HashMap();
		for( object in objects ) clones.put( object, target.duplicate( object ) );
		for( connection in canvasItem.getConnections() ) {
			def inputComponent = connection.getInputTerminal().getTerminalHolder() as ComponentItem;
			def outputComponent = connection.getOutputTerminal().getTerminalHolder() as ComponentItem;
			if( Sequences.indexOf( objects, inputComponent ) >= 0 and Sequences.indexOf( objects, outputComponent ) >= 0 ) {
				def outputTerminal = (clones.get( outputComponent ) as CanvasObjectItem).getTerminalByLabel( connection.getOutputTerminal().getLabel() ) as OutputTerminal;
				def inputTerminal = (clones.get( inputComponent ) as CanvasObjectItem).getTerminalByLabel( connection.getInputTerminal().getLabel() ) as InputTerminal;
				target.connect( outputTerminal, inputTerminal );
			}
		}
		if( deleteInitial )
			for( object in objects ) object.delete();
		
		for( clone in clones.values() ) clone as CanvasObjectItem;
	}
	
	protected def contextMenu:PopupMenu = PopupMenu {
		items: [
			cloneAction,
			deleteAction,
			moveSubmenu,
			addNoteAction
		]
		onShowing: function():Void {
			deleteAction.disable = sizeof Selectable.selects[d|d instanceof Deletable] == 0;
			cloneAction.disable = sizeof Selectable.selects[c|c instanceof CanvasObjectNode] == 0;
		}
	}
	
	init {
		addMouseHandler( MOUSE_DRAGGED, onMouseDragged );
		addMouseHandler( MOUSE_PRESSED, onMouseDown );
		addMouseHandler( MOUSE_RELEASED, onMouseUp );
	}
	
	var contextMouseEvent:MouseEvent;
	
	override function create() {
		Group {
			content: [
				Rectangle {
					fill: Color.TRANSPARENT
					width: bind width
					height: bind height
					onMouseClicked: function( e:MouseEvent ) {
						if( e.button == MouseButton.PRIMARY and not e.controlDown ) {
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
						refreshTerminals();
					}
					onMouseClicked: function( e:MouseEvent ) {
						if( e.button == MouseButton.SECONDARY ) {
							contextMouseEvent = e;
							contextMenu.show( this, e.screenX, e.screenY );
						}
					}
				}, contextMenu
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
			componentLayer.content = dummyNodeComponents;
			connectionLayer.content = dummyNodeConnections;
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
			
			noteLayer.content = [ dummyNodeNotes, Note.loadNotes( this ) ];
				
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
		def objects = [ componentLayer.content[o|o instanceof CanvasObjectNode], noteLayer.content[o|o instanceof Note] ];
		if( sizeof objects == 0 ) {
			areaWidth = width as Integer;
			areaHeight = height as Integer;
			return;
		}
		
		//log.debug( "Recalculating canvas area ( {areaWidth}, {areaHeight} )" );
		var minX = areaWidth;
		var minY = areaHeight;
		var maxX = 0;
		var maxY = 0;
		
		for( obj in objects ) {
			minX = Math.min( minX, obj.layoutX as Integer );
			minY = Math.min( minY, obj.layoutY as Integer );
			maxX = Math.max( maxX, obj.layoutX + obj.layoutBounds.width as Integer );
			maxY = Math.max( maxY, obj.layoutY + obj.layoutBounds.height as Integer );
		}
		
		def shiftX = if( padding - minX < 0 )
			Math.min( Math.max( padding - minX, (width as Integer) - ( maxX + padding ) ), 0 )
		else padding - minX;
		
		def shiftY = if( padding - minY < 0 )
			Math.min( Math.max( padding - minY, (height as Integer) - ( maxY + padding ) ), 0 )
		else padding - minY;
		
		for( obj in objects ) {
			obj.layoutX += shiftX;
			obj.layoutY += shiftY;
		}
		
		areaWidth = Math.max( width as Integer, maxX + shiftX + padding );
		areaHeight = Math.max( height as Integer, maxY + shiftY + padding );
		
		offsetX = Math.max( 0, Math.min( offsetX + shiftX, areaWidth - width as Integer ) );
		offsetY = Math.max( 0, Math.min( offsetY + shiftY, areaHeight - height as Integer ) );
		
		refreshTerminals();
		
		//log.debug( "Done recalculating Canvas area! New width, height = (\{\}, \{\})", areaWidth, areaHeight );
	}
	
	/**
	 * Locates and returns the CanvasObjectNode for the ModelItem with the given id.
	 */
	public function lookupCanvasNode( id:String ):CanvasObjectNode {
		componentLayer.lookup( id ) as CanvasObjectNode;
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
					runInFxThread( function() { addComponent( event.getElement() as ComponentItem ) } );
				} else {
					runInFxThread( function() { removeModelItem( event.getElement() as ComponentItem ) } );
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
	
	var sStartX:Number;
	var sStartY:Number;
	var sOffsetX:Number;
	var sOffsetY:Number;
	var sDragging = false;
	def selectionRect = Rectangle {
		fill: Color.rgb( 0x2c, 0x57, 0xfe, 0.3 )
		stroke: Color.rgb( 0x2c, 0x57, 0xfe )
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
		} else {
			sDragging = true;
			sStartX = e.sceneX;
			sStartY = e.sceneY;
			
			//Due to Swing related bug, use this instead of e.sceneX, e.sceneY:
			def location = java.awt.MouseInfo.getPointerInfo().getLocation();
			sOffsetX = location.x - e.sceneX;
			sOffsetY = location.y - e.sceneY;
			
			selectionRect.x = sStartX;
			selectionRect.y = sStartY;
			selectionRect.width = 0;
			selectionRect.height = 0;
			insert selectionRect into AppState.overlay;
		}
	}
	
	function onMouseUp(e:MouseEvent):Void {
		if( cDragging ) {
			cDragging = false;
			cursor = Cursor.DEFAULT;
		} else if( sDragging ) {
			sDragging = false;
			delete selectionRect from AppState.overlay;
		}
	}
	
	function onMouseDragged(e:MouseEvent):Void {
		if( cDragging ) {
			def dX = e.sceneX - cStartX as Integer;
			def dY = e.sceneY - cStartY as Integer;
	
			offsetX = Math.max( 0, Math.min( offsetX - dX, areaWidth - width as Integer ) );
			offsetY = Math.max( 0, Math.min( offsetY - dY, areaHeight - height as Integer ) );
	
			cStartX = e.sceneX;
			cStartY = e.sceneY;
			
			refreshTerminals();
		} else if( sDragging ) {
			//Due to Swing related bug, use this instead of e.sceneX, e.sceneY:
			def location = java.awt.MouseInfo.getPointerInfo().getLocation();
			def deltaX = location.x - sOffsetX;
			def deltaY = location.y - sOffsetY;
			
			selectionRect.x = Math.min( deltaX, sStartX );
			selectionRect.y = Math.min( deltaY, sStartY );
			selectionRect.width = Math.abs( deltaX - sStartX );
			selectionRect.height = Math.abs( deltaY - sStartY );
			Selectable.selectNone();
			for( node in [componentLayer.content, connectionLayer.content][n|n instanceof Selectable] )
				if( node.localToScene( node.boundsInLocal ).intersects( selectionRect.layoutBounds ) )
					(node as Selectable).select();
		}
	}
}
