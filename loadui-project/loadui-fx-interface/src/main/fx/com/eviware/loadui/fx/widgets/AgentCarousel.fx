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
package com.eviware.loadui.fx.widgets;

import javafx.scene.Node;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.util.Sequences;

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.PopupMenu;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.carousel.Carousel;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.dnd.DroppableNode;
import com.eviware.loadui.fx.widgets.toolbar.AgentToolbarItem;
import com.eviware.loadui.fx.dialogs.CreateNewAgentDialog;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.AgentItem;

import java.lang.RuntimeException;

public class AgentCarousel extends DroppableNode, Resizable, EventHandler {
	/**
	 * A reference to the current Workspace.
	 */
	public-init var workspace: WorkspaceItem;
	
	override function handleEvent( e ) {
		def event = e as CollectionEvent;
		if( event.getKey().equals( WorkspaceItem.AGENTS ) ) {
			if( event.getEvent() == CollectionEvent.Event.ADDED ) {
				runInFxThread( function() { addAgent( event.getElement() as AgentItem ) } );
			} else {
				runInFxThread( function() { removeAgent( event.getElement() as AgentItem ) } );
			}
		}
	}
	
	def popup = PopupMenu {
		items: [
			MenuItem {
				text: "New Agent"
				action: function() {
					CreateNewAgentDialog { workspace: workspace };
				}
			}
		]
	}
	
	def carousel = Carousel {
		label: "Agents"
		layoutInfo: LayoutInfo { vfill: true, hfill: true, vgrow: Priority.ALWAYS, hgrow: Priority.ALWAYS }
		widthFactor: 0.8
		onMousePressed: function( e ) {
			if( e.popupTrigger ) {
				popup.show( this, e.screenX, e.screenY );
			}
		}
		onMouseReleased: function( e ) {
			if( e.popupTrigger ) {
				popup.show( this, e.screenX, e.screenY );
			}
		}
		onMouseEntered: function( e ) { this.onMouseEntered( e ) }
		onMouseExited: function( e ) { this.onMouseExited( e ) }
	}
	
	def stack = Stack { content: [ carousel, popup ], width: bind width, height: bind height }
	
	override function create() {
		stack
	}
	
	override function getPrefWidth( height ) { stack.getPrefWidth( height ) }
	override function getPrefHeight( width ) { stack.getPrefHeight( width ) }
	
	override var accept = function( d ) {
		d.node instanceof AgentToolbarItem
	}
	
	override var onDrop = function( d ) {
		if ( d.node instanceof AgentToolbarItem ) {
			CreateNewAgentDialog { workspace: workspace };
		}
	}
	
	postinit {
		if( workspace == null )
			throw new RuntimeException( "Workspace must not be null!" );
		
		workspace.addEventListener( CollectionEvent.class, this );
		
		for( agent in workspace.getAgents() )
			addAgent( agent );
			
		if( sizeof carousel.items > 0 ) carousel.select( carousel.items[0] );
	}
	
	function addAgent( agent:AgentItem ):Void {
		def node = DraggableFrame { draggable: AgentNode { agent: agent } };
		carousel.items = Sequences.sort( [ carousel.items, node ], COMPARE_BY_TOSTRING ) as Node[];
		carousel.select( node );
	}
	
	function removeAgent( agent:AgentItem ):Void {
		for( node in carousel.items[f|f instanceof DraggableFrame] ) {
			def draggable = (node as DraggableFrame).draggable;
			if( (draggable as AgentNode).agent == agent )
				delete node from carousel.items;
		}
	}
}