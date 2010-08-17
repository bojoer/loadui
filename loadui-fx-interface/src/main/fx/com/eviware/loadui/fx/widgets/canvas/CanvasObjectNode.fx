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
*CanvasObjectNode.fx
*
*Created on aug 16, 2010, 10:08:15 fm
*/

package com.eviware.loadui.fx.widgets.canvas;

import javafx.scene.Node;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.fxd.FXDNode;

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.MenuButton;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Movable;
import com.eviware.loadui.fx.widgets.ModelItemHolder;
import com.eviware.loadui.fx.ui.resources.DialogPanel;
import com.eviware.loadui.fx.dialogs.RenameModelItemDialog;
import com.eviware.loadui.fx.dialogs.DeleteModelItemDialog;

import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;

import java.util.EventObject;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.canvas.CanvasObjectNode" );

def ledActive = Image { url: "{__ROOT__}images/png/led-active.png" };
def ledInactive = Image { url: "{__ROOT__}images/png/led-inactive.png" };

public class CanvasObjectNode extends BaseNode, Movable, Selectable, ModelItemHolder, EventHandler {
	
	var inputs:TerminalNode[];
	var outputs:TerminalNode[];
	
	public-init protected var canvasObject:CanvasObjectItem on replace oldCanvasObject {
		modelItem = canvasObject;
		oldCanvasObject.removeEventListener( BaseEvent.class, this );
		if( canvasObject != null ) {
			canvasObject.addEventListener( BaseEvent.class, this );
			label = canvasObject.getLabel();
			active = canvasObject.isActive();
			def terminals = for( terminal in canvasObject.getTerminals() ) TerminalNode { id: terminal.getId(), canvas: canvas, terminal: terminal, fill: bind color };
			inputs = terminals[t|t.terminal instanceof InputTerminal];
			outputs = terminals[t|not (t.terminal instanceof InputTerminal)];
			layoutX = Integer.parseInt( canvasObject.getAttribute( "gui.layoutX", "0" ) );
			layoutY = Integer.parseInt( canvasObject.getAttribute( "gui.layoutY", "0" ) );
			canvas.refreshTerminals();
		}
	}
	
	public-init var canvas:Canvas;
	
	public var menuItems: Node[] = [
		MenuItem {
			text: ##[RENAME]"Rename"
			action: function() { RenameModelItemDialog { modelItem: canvasObject } }
		}, MenuItem {
			text: ##[CLONE]"Clone"
			action: function() { onClone() }
		}, MenuItem {
			text: ##[DELETE]"Delete"
			action: function() { DeleteModelItemDialog { modelItem: canvasObject } }
		}, Separator {
		}, MenuItem {
			text: ##[SETTINGS]"Settings"
			action: function() { onSettings() }
		}
	];
	
	public var onSettings: function():Void;
	public var onClone: function():Void;
	
	public-read protected var colorStr:String = "#26a8f9" on replace {
		color = Color.web( colorStr );
	}
	
	public-read var color:Color;
	
	public-read var label:String;
	
	public-read var active:Boolean;
	
	public-read var toolbar:HBox;
	
	public-read var body:VBox;
	
	override function release() {
		canvasObject = null;
	}
	
	override var styleClass = "canvas-object-node";
	
	override var layoutX on replace {
		modelItem.setAttribute( "gui.layoutX", "{layoutX as Integer}" );
	}
	
	override var layoutY on replace {
		modelItem.setAttribute( "gui.layoutY", "{layoutY as Integer}" );
	}
	
	override var blocksMouse = true;
	
	override var onGrab = function():Void {
		toFront();
		requestFocus();
		if( mouseEvent.controlDown ) { if( selected ) deselect() else select() } else if( not selected ) selectOnly();
	}
	
	override var onMove = function() { canvas.refreshComponents() };
	
	override var onDragging = function() { canvas.refreshTerminals() };
	
	protected function onReloaded():Void {};
	
	override function create():Node {
		addMouseHandler( MOUSE_CLICKED, function( e:MouseEvent ) {
			if( e.button == MouseButton.SECONDARY ) {
				canvas.openContextMenu( e.screenX, e.screenY );
			}
		} );
		
		var menuButton:MenuButton;
		
		DialogPanel {
			titlebarColor: bind "{colorStr}";
			highlight: bind selected
			body: VBox {
				padding: Insets { left: 13, right: 13 }
				content: [
					HBox {
						layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, height: 31, maxHeight: 31, margin: Insets { top: -14, bottom: -7 } }
						nodeHPos: HPos.CENTER
						content: bind inputs
					}, Label {
						graphic: ImageView {
							image: bind if( active ) ledActive else ledInactive
						}
						text: bind label
					}, toolbar = HBox {
						styleClass: "canvas-object-toolbar"
						nodeVPos: VPos.CENTER
						layoutInfo: LayoutInfo { height: 19, maxHeight: 19, margin: Insets { top: 3, left: 10 } }
						content: [
							menuButton = MenuButton {
								styleClass: bind if( menuButton.showing ) "menu-button-showing" else "menu-button"
								text: "Menu"
								tooltip: Tooltip { text: bind label }
								items: bind menuItems
							}, Separator { //Gap
								styleClass: ""
								layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS }
							}, Separator {
								vertical: true
							}, Button {
								graphic: FXDNode { url: "{__ROOT__}images/component_wrench_icon.fxz" }
								tooltip: Tooltip { text: "Settings" }
								action: function() { onSettings() }
							}, Separator {
								vertical: true
							}, Button {
								graphic: FXDNode { url: "{__ROOT__}images/component_help_icon.fxz" }
								tooltip: Tooltip { text: "Open Help page" }
								action: function() { openURL( modelItem.getHelpUrl() ) }
							}
						]
					}, body = VBox {
						layoutInfo: LayoutInfo { hfill: true, vfill: true, hgrow: Priority.ALWAYS, vgrow: Priority.ALWAYS }
						nodeHPos: HPos.CENTER
						nodeVPos: VPos.CENTER
					}, HBox {
						layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, height: 31, maxHeight: 31, margin: Insets { top: 14, bottom: -14 } }
						nodeHPos: HPos.CENTER
						content: bind outputs
					}
				]
			}
		}
	}
	
	override function handleEvent( e:EventObject ) {
		if( e instanceof CollectionEvent ) {
			def event = e as CollectionEvent;
			if( CanvasObjectItem.TERMINALS.equals( event.getKey() ) ) {
				if( event.getEvent() == CollectionEvent.Event.ADDED ) {
					runInFxThread( function() { addTerminal( event.getElement() as Terminal ) } );
				} else {
					runInFxThread( function() { removeTerminal( event.getElement() as Terminal ) } );
				}
			}
		} else if( e instanceof BaseEvent ) {
			def event = e as BaseEvent;
			if( event.getKey() == CanvasObjectItem.LABEL ) {
				runInFxThread( function():Void { label = canvasObject.getLabel() } );
			} else if( event.getKey() == CanvasObjectItem.ACTIVITY ) {
				runInFxThread( function():Void { active = canvasObject.isActive() } );
			} else if( event.getKey() == CanvasObjectItem.RELOADED ) {
				runInFxThread( function():Void { onReloaded() } );
			}
		}
	}
	
	public function lookupTerminalNode( id:String ):TerminalNode {
		for( tn in [ inputs, outputs ] ) {
			if( tn.id == id )
				return tn;
		}
		
		null
	}
	
	function addTerminal( terminal:Terminal ) {
		def tNode = TerminalNode { id: terminal.getId(), canvas: canvas, terminal: terminal, fill: bind color };
		if( terminal instanceof InputTerminal ) {
			insert tNode into inputs;
		} else {
			insert tNode into outputs;
		}
		canvas.refreshTerminals();
	}
	
	function removeTerminal( terminal:Terminal ) {
		if( terminal instanceof InputTerminal ) {
			delete lookupTerminalNode( terminal.getId() ) from inputs;
		} else {
			delete lookupTerminalNode( terminal.getId() ) from outputs;
		}
		canvas.refreshTerminals();
	}
}
