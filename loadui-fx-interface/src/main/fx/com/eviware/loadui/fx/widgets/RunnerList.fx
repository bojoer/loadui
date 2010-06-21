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
*RunnerList.fx
*
*Created on feb 10, 2010, 09:32:42 fm
*/

package com.eviware.loadui.fx.widgets;

import javafx.util.Sequences;
import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.layout.Resizable;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.pagelist.PagelistControl;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.DroppableNode;
import com.eviware.loadui.fx.dialogs.CreateNewRunnerDialog;
import com.eviware.loadui.fx.widgets.toolbar.RunnerToolbarItem;

import com.eviware.loadui.fx.ui.popup.ActionMenuItem;
import com.eviware.loadui.fx.ui.popup.PopupMenu;
import com.eviware.loadui.fx.runners.discovery.RunnerDiscoverer;
import com.eviware.loadui.fx.runners.discovery.RunnerDiscovererDialog;
import com.eviware.loadui.fx.ui.popup.SeparatorMenuItem;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;

import java.util.EventObject;
import java.util.Comparator;
import java.lang.RuntimeException;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.RunnerItem;
import org.slf4j.LoggerFactory;
public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.RunnerList" );

/**
 * A list of all the Runners in the current Workspace.
 */
public class RunnerList extends CustomNode, Resizable, EventHandler {

	/**
	 * A reference to the current Workspace.
	 */
	public-init var workspace: WorkspaceItem;
	
	var pagelist:PagelistControl;

	override function getPrefHeight( width:Float ) {
		295
	}
	
	override function getPrefWidth( height:Float ) {
		pagelist.getPrefWidth( height );
	}
	
	override function handleEvent( e:EventObject ) {
		def event = e as CollectionEvent;
		if( event.getKey().equals( WorkspaceItem.RUNNERS ) ) {
			if( event.getEvent() == CollectionEvent.Event.ADDED ) {
				runInFxThread( function() { addRunner( event.getElement() as RunnerItem ) } );
			} else {
				runInFxThread( function() { removeRunner( event.getElement() as RunnerItem ) } );
			}
		}
	}
	
	postinit {
		if( workspace == null )
			throw new RuntimeException( "Workspace must not be null!" );
		
		workspace.addEventListener( CollectionEvent.class, this );
		
		for( runner in workspace.getRunners() )
			addRunner( runner );
	}
	
	function addRunner( runner:RunnerItem ):Void {
		pagelist.content = Sequences.sort( [ pagelist.content, DraggableFrame { draggable:RunnerNode { runner: runner } } ], COMPARE_BY_TOSTRING ) as Node[];
	}
	
	function removeRunner( runner:RunnerItem ):Void {
		for( node in pagelist.content[f|f instanceof DraggableFrame] ) {
			def draggable = (node as DraggableFrame).draggable;
			if( (draggable as RunnerNode).runner == runner )
				delete node from pagelist.content;
		}
	}

	override function create() {
		def popup = PopupMenu {};
		popup.items = [
			ActionMenuItem {
				text: "Detect Agents"
				action: function() {
					RunnerDiscovererDialog{}.show();
				}
			}
			SeparatorMenuItem{}
			ActionMenuItem {
				text: "New Agent"
				action: function() {
					CreateNewRunnerDialog{ workspace: workspace };
				}
			}
		];
		
		pagelist = PagelistControl {
			text: ##[AGENTS]"AGENTS"
			height: bind height
			width: bind width
			onMousePressed: function(e: MouseEvent){
				if(e.popupTrigger){
					popup.layoutX = e.sceneX;
					popup.layoutY = e.sceneY;
					popup.open();
				}
			}
			onMouseReleased: function(e: MouseEvent){
				if(e.popupTrigger){
					popup.layoutX = e.sceneX;
					popup.layoutY = e.sceneY;
					popup.open();
				}
			}
		}
		DroppableNode {
			contentNode: pagelist
			accept: function( d:Draggable ) {
				d.node instanceof RunnerToolbarItem
			}
			onDrop: function( d:Draggable ) {
				if ( d.node instanceof RunnerToolbarItem ) {
					log.debug( "Opening CreateNewRunnerDialog..." );
					CreateNewRunnerDialog { workspace: workspace };
				}
			}
		}
	}
}
