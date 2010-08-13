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
*ComponentNode.fx
*
*Created on feb 24, 2010, 14:02:20 em
*/

package com.eviware.loadui.fx.widgets.canvas;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.layout.Container;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Stack;
import javafx.geometry.VPos;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.fxd.FXDNode;
import javafx.util.Math;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.StylesheetAware;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Movable;
import com.eviware.loadui.fx.ui.popup.Menu;
import com.eviware.loadui.fx.ui.popup.PopupMenu;
import com.eviware.loadui.fx.ui.popup.ActionMenuItem;
import com.eviware.loadui.fx.ui.resources.TitlebarPanel;
import com.eviware.loadui.fx.ui.resources.MenuArrow;
import com.eviware.loadui.fx.ui.resources.Paints;
import com.eviware.loadui.fx.ui.button.GlowButton;
import com.eviware.loadui.fx.dialogs.DeleteModelItemDialog;
import com.eviware.loadui.fx.dialogs.RenameModelItemDialog;
import com.eviware.loadui.fx.widgets.ModelItemHolder;
import com.eviware.loadui.fx.ui.menu.button.MenuBarButton;
import com.eviware.loadui.fx.ui.dialogs.*;
import com.eviware.loadui.fx.ui.layout.LayoutComponentNode;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.component.categories.TriggerCategory;
import com.eviware.loadui.api.model.CanvasObjectItem;

import java.util.EventObject;
import java.lang.RuntimeException;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.fx.ui.border.*;

public function create( component:ComponentItem, canvas:Canvas ):ComponentNode {
	if( TriggerCategory.CATEGORY.equalsIgnoreCase( component.getCategory() ) )
		TriggerComponentNode { component: component, canvas: canvas, id: component.getId() }
	else
		ComponentNode { component: component, canvas: canvas, id: component.getId() }
}

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.ComponentNode" );

/**
 * Node to be displayed in a Canvas, representing a ComponentItem.
 * It can be moved around the Canvas, and its position will be stored in the project file.
 * 
 * @author dain.nilsson
 */
public class ComponentNode extends CanvasNode {
	/**
	 * The ComponentItem to display.
	 */
	public-init var component:ComponentItem on replace { color = Color.web( component.getColor() ); }
	
	override var modelItem = bind lazy component;
	
	override var settingsAction = function():Void {
		DefaultComponentSettingsPanel {
			component: component
		}.show();
	}
	
	var inputs:TerminalNode[];
	var outputs:TerminalNode[];
	
	init {
		if( not FX.isInitialized( component ) )
			throw new RuntimeException( "ComponentNode cannot be initialized without setting component!" );
		
		for( terminal in component.getTerminals() )
			addTerminal( terminal );
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
	
	override var width = bind Math.max( 200, Math.max( 20 + menuContent.layoutBounds.width,
			face.getPrefWidth( -1 ) + 30 ) ) on replace oldVal = newVal {
		face.width = width - 30;
	}
	
	override var height = bind Math.max( 100, faceGroup.boundsInLocal.height + 60 );
	
	protected var roundedFrame:Node;
	
	var active: Boolean = component.isActive();
	
	var menuContent:HBox;
	var face:LayoutComponentNode;
	def faceGroup = Group { content: bind face, layoutX: 15, layoutY: 20 };
	override function create() {
		if( not FX.isInitialized(roundedFrame) ) {
			roundedFrame = Rectangle {
				layoutX: 15
				layoutY: 20
				arcHeight: 14
				arcWidth: 14
				width: bind width - 30
				height: bind height - 50
				stroke: bind roundedFrameStroke
				fill: bind roundedFrameFill
			}
		}
	
		face = LayoutComponentNode.buildLayoutComponentNode( component.getLayout() );
		def baseRect = Rectangle { fill: Color.TRANSPARENT, width: bind width, height: bind Math.max( 100, height - 5 ) };
		
		RoundedRectBorder {
		    arc: 30
		    accent: Color.TRANSPARENT
		    borderColor: bind if ( selected ) Color.web("#535353", .6) else Color.TRANSPARENT
		    borderWidth: 0
		    backgroundFill: bind if ( selected ) Color.web("#535353", .6) else Color.TRANSPARENT
		    borderLeftWidth: 10
		    borderRightWidth: 10
		    borderTopWidth: if ( inputs == null ) 10 else 0
		    borderBottomWidth: if ( outputs == null ) 10 else 0
		    base: Color.TRANSPARENT
			node: Group {
				content: [
					handle = BaseNode {
						contentNode: TitlebarPanel {
							backgroundFill: bind backgroundFill
							content: [
								baseRect,
								toolbarBoxLeft,
								toolbarBoxRight,
								roundedFrame,
								faceGroup,
								Rectangle {
									layoutY: bind height - 20
									height: 20
									width: bind width
									fill: bind footerFill
								}
							]
							titlebarColor: color
							//titlebarEffect: bind if( selected ) Selectable.effect else null
							titlebarContent: [
								ImageView {
									layoutX: 14
									layoutY: 14
									image: Image {
						            	url: "{__ROOT__}images/png/led-active.png"
						        	}
						        	visible: bind active 
								}
								ImageView {
									layoutX: 14
									layoutY: 14
									image: Image {
						            	url: "{__ROOT__}images/png/led-inactive.png"
						        	}
						        	visible: bind not active
								}
								Label {
									text: bind label.toUpperCase()
									opacity: 0.5
									textFill: Color.BLACK
									font: Font {
									    size: 9
									}
									layoutX: 26
									layoutY: 2
									width: bind width - 30
									height: bind 30
								}
							]
						}
					},
					TerminalContainer { width: bind width, layoutY: -8, content: bind inputs },
					TerminalContainer { width: bind width, layoutY: bind height + 23, content: bind outputs }
				]
			}
		}
	}
	
	override function handleEvent( e:EventObject ) {
		super.handleEvent( e );
		if( e instanceof CollectionEvent ) {
			def event = e as CollectionEvent;
			if( ComponentItem.TERMINALS.equals( event.getKey() ) ) {
				if( event.getEvent() == CollectionEvent.Event.ADDED ) {
					runInFxThread( function() { addTerminal( event.getElement() as Terminal ) } );
				} else {
					runInFxThread( function() { removeTerminal( event.getElement() as Terminal ) } );
				}
			}
		}
		if(e instanceof BaseEvent) {
			def event = e as BaseEvent;
			if(CanvasObjectItem.ACTIVITY.equals(event.getKey())) {
				runInFxThread( function() { setActive(component.isActive()) } );
			}
		}
	}
	
	function setActive(a: Boolean): Void {
		active = a;
	}
	
	override function onReloaded():Void {
		face.release();
		face = LayoutComponentNode.buildLayoutComponentNode( component.getLayout() );
	}
	
	override function release() {
		component = null;
		inputs = [];
		outputs = [];
		face.release();
		face = null;
	}
}

class TerminalContainer extends Container {
	override var content on replace {
		doLayout();
	}
	
	override function doLayout() {
		def managed = getManaged( content );
		def spacing = width / ( sizeof managed + 1 );
		var offsetX = spacing;
		for( node in managed ) {
			node.layoutY = height / 2;
			node.layoutX = offsetX;
			offsetX += spacing;
		}
	}
}
