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
*ProjectList.fx
*
*Created on feb 8, 2010, 16:16:06 em
*/

package com.eviware.loadui.fx.widgets;

import javafx.util.Sequences;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.CustomNode;
import javafx.scene.layout.Resizable;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.pagelist.PageList;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.DroppableNode;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.CollectionEvent;

import java.io.File;
import java.util.EventObject;
import java.lang.RuntimeException;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.fx.widgets.toolbar.ProjectToolbarItem;
import com.eviware.loadui.fx.dialogs.CreateNewProjectDialog;
import org.slf4j.LoggerFactory;
import com.eviware.loadui.api.events.BaseEvent;

import com.javafx.preview.control.PopupMenu;
import com.javafx.preview.control.MenuItem;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import java.awt.MouseInfo;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.ProjectList" );

/**
 * A list of all the Projects in the current Workspace.
 */
public class ProjectList extends BaseNode, Droppable, Resizable, EventHandler {
	
	/**
	* A reference to the current Workspace.
	*/
	public-init var workspace: WorkspaceItem;
	
	var pagelist:PageList;

	override function getPrefHeight( width:Float ) {
		pagelist.getPrefHeight( width );
	}
	
	override function getPrefWidth( height:Float ) {
		pagelist.getPrefWidth( height );
	}
	
	override function handleEvent( e:EventObject ) {
		if(e instanceof CollectionEvent) {
			def event = e as CollectionEvent;
			if( event.getKey().equals( WorkspaceItem.PROJECT_REFS ) ) {
				if( event.getEvent() == CollectionEvent.Event.ADDED ) {
					log.debug("Project added!!!!! {e.getSource()}" );
					runInFxThread( function() { 
						addProjectRef( event.getElement() as ProjectRef );
					} );
				} else {
					runInFxThread( function() { 
						removeProjectRef( event.getElement() as ProjectRef );
					} );
				}
			}
		}
	}
	
	postinit {
		if( workspace == null )
			throw new RuntimeException( "Workspace must not be null!" );
		
		workspace.addEventListener( CollectionEvent.class, this );
		
		for( ref in workspace.getProjectRefs() ) 
			addProjectRef( ref )
	}
	
	function addProjectRef( ref:ProjectRef ):Void {
		pagelist.items = Sequences.sort( [ pagelist.items, DraggableFrame { draggable:ProjectNode { projectRef: ref } } ], COMPARE_BY_TOSTRING ) as Node[];
	}
	
	public function checkExistingProjects() {
		for( ref in workspace.getProjectRefs() ) {
			if ( not ref.getProjectFile().exists() ) {
				var dialog:Dialog = Dialog {
					x: 300
					y: 300
					title: "Error loading project: {ref.getLabel()}"
					content: [
						javafx.scene.control.Label { text: "Project file does not exists, do you want to be removed from workspace?"}
					]
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
	
	function removeProjectRef( ref:ProjectRef ):Void {
		for( node in pagelist.items[f|f instanceof DraggableFrame] ) {
			def draggable = (node as DraggableFrame).draggable;
			if( (draggable as ProjectNode).projectRef == ref )
				delete node from pagelist.items;
		}
	}
	
	var x:Number;
	var y:Number;
	
	override function create():Node {
		def popup = PopupMenu {
			items: [
				MenuItem {
					text: "New Project"
					action: function() {
						CreateNewProjectDialog { 
							workspace: workspace
						};
					}
				}
			]
		};
		
		pagelist = PageList {
			label: ##[PROJECTS]"PROJECTS"
			height: bind height
			width: bind width
			onMousePressed: function(e: MouseEvent){
				if(e.popupTrigger){
					popup.show( this, e.screenX, e.screenY );
				}
			}
			onMouseReleased: function(e: MouseEvent){
				if(e.popupTrigger){
					popup.show( this, e.screenX, e.screenY );
				}
			}
		}
	}
	
	override var accept = function( d:Draggable ) {
		d.node instanceof ProjectToolbarItem
	}
	
	override var onDrop = function( d:Draggable ) {
		log.debug("{d} dropped!");
		if (d.node instanceof ProjectToolbarItem) {
			log.debug( "Opening CreateNewProjectDialog..." );
			CreateNewProjectDialog { 
				workspace: workspace 
				layoutX: (d.node as ProjectToolbarItem).translateX - 105
				layoutY: (d.node as ProjectToolbarItem).translateY + 95
			};
		}
	}
}
