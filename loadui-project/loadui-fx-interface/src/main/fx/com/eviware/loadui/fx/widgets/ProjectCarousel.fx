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
import javafx.scene.control.Label;
import javafx.util.Sequences;

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.PopupMenu;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.carousel.Carousel;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.dnd.DroppableNode;
import com.eviware.loadui.fx.widgets.toolbar.ProjectToolbarItem;
import com.eviware.loadui.fx.dialogs.CreateNewProjectDialog;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.ProjectRef;

import java.lang.RuntimeException;

public class ProjectCarousel extends DroppableNode, Resizable, EventHandler {
	/**
	 * A reference to the current Workspace.
	 */
	public-init var workspace: WorkspaceItem;
	
	override function handleEvent( e ) {
		def event = e as CollectionEvent;
		if( event.getKey().equals( WorkspaceItem.PROJECT_REFS ) ) {
			if( event.getEvent() == CollectionEvent.Event.ADDED ) {
				runInFxThread( function() { addProjectRef( event.getElement() as ProjectRef ) } );
			} else {
				runInFxThread( function() { removeProjectRef( event.getElement() as ProjectRef ) } );
			}
		}
	}
	
	def popup = PopupMenu {
		items: [
			MenuItem {
				text: "New Project"
				action: function() {
					CreateNewProjectDialog { workspace: workspace };
				}
			}
		]
	}
	
	def carousel = Carousel {
		label: "Project"
		layoutInfo: LayoutInfo { vfill: true, hfill: true, vgrow: Priority.ALWAYS, hgrow: Priority.ALWAYS }
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
	}
	
	def stack = Stack { content: [ carousel, popup ], width: bind width, height: bind height }
	
	override function create() {
		stack
	}
	
	override function getPrefWidth( height ) { stack.getPrefWidth( height ) }
	override function getPrefHeight( width ) { stack.getPrefHeight( width ) }
	
	override var accept = function( d ) {
		d.node instanceof ProjectToolbarItem
	}
	
	override var onDrop = function( d ) {
		if ( d.node instanceof ProjectToolbarItem ) {
			CreateNewProjectDialog { workspace: workspace };
		}
	}
	
	postinit {
		if( workspace == null )
			throw new RuntimeException( "Workspace must not be null!" );
		
		workspace.addEventListener( CollectionEvent.class, this );
		
		for( project in workspace.getProjectRefs() )
			addProjectRef( project );
	}
	
	public function checkExistingProjects() {
		for( ref in workspace.getProjectRefs() ) {
			if ( not ref.getProjectFile().exists() ) {
				var dialog:Dialog = Dialog {
					x: 300
					y: 300
					title: "Error loading project: {ref.getLabel()}"
					content: Label { text: "Project file does not exists, do you want to be removed from workspace?"}
					okText: "Ok"
					onOk: function() {
						workspace.removeProject( ref );
						dialog.close();
					}
					onCancel: function() {
						dialog.close()
					}
				}
			}
		}
	}
	
	function addProjectRef( ref:ProjectRef ):Void {
		def node = DraggableFrame { draggable:ProjectNode { projectRef: ref } };
		carousel.items = Sequences.sort( [ carousel.items, node ], COMPARE_BY_TOSTRING ) as Node[];
		carousel.select( node );
	}
	
	function removeProjectRef( ref:ProjectRef ):Void {
		for( node in carousel.items[f|f instanceof DraggableFrame] ) {
			def draggable = (node as DraggableFrame).draggable;
			if( (draggable as ProjectNode).projectRef == ref )
				delete node from carousel.items;
		}
	}
}