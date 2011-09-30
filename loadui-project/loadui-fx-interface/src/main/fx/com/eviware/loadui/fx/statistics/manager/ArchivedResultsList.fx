/* 
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.fx.statistics.manager;

import javafx.scene.Node;
import javafx.scene.layout.Resizable;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.util.ModelUtils;
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.pagelist.PageList;

import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.statistics.ProjectExecutionManager;
import com.eviware.loadui.api.statistics.ExecutionAddon;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.util.BeanInjector;

import java.util.Comparator;

public class ArchivedResultsList extends BaseNode, Resizable, Droppable {
	def pagelist = PageList { width: bind width, height: bind height, label: "Archived Results", comparator: new ExecutionComparator() };
	def listener = new ExecutionsListener();
	def executionListener = new ExecutionListener();
	
	def projectExecutionManager:ProjectExecutionManager = BeanInjector.getBean( ProjectExecutionManager.class );
	
	def manager:ExecutionManager = BeanInjector.getBean( ExecutionManager.class ) on replace {
		manager.addEventListener( CollectionEvent.class, listener );
	}
	
	def project = bind StatisticsWindow.instance.project on replace {
		for( item in pagelist.items ) {
			((item as DraggableFrame).draggable as ResultNode).execution.removeEventListener( BaseEvent.class, executionListener );
		}
		pagelist.items = [];
		if( project != null ) {
			for( execution in project.getAddon( ExecutionAddon.class ).getExecutions() ) {
				execution.addEventListener( BaseEvent.class, executionListener );
				if( execution.isArchived() and pagelist.lookup( execution.getId() ) == null ) {
					insert DraggableFrame { draggable: ResultNode { execution: execution }, id: execution.getId() } before pagelist.items[0];
				}
			}
		}
	}
	
	override var accept = function( draggable:Draggable ) {
		if( draggable instanceof ResultNode )
		{
			return not (draggable as ResultNode).execution.isArchived();
		}
		return false;
	}
	
	override var onDrop = function( draggable:Draggable ) {
		(draggable as ResultNode).execution.archive();
	}
	
	override function create():Node {
		pagelist
	}
	
	override function getPrefHeight( width:Number ) {
		pagelist.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ) {
		pagelist.getPrefWidth( height )
	}
}

class ExecutionListener extends WeakEventHandler {
	override function handleEvent( e ):Void {
		def event = e as BaseEvent;
		def execution = event.getSource() as Execution;
		FxUtils.runInFxThread( function() {
			if( Execution.ARCHIVED.equals( event.getKey() ) and pagelist.lookup( execution.getId() ) == null ) {
				insert DraggableFrame { draggable: ResultNode { execution: execution }, id: execution.getId() } before pagelist.items[0];
			}
			else if( Execution.DELETED.equals( event.getKey() ) ) {
				execution.removeEventListener( BaseEvent.class, executionListener );
				delete pagelist.lookup( execution.getId() ) from pagelist.items;
			}
		});
	}
}

class ExecutionsListener extends WeakEventHandler {
	override function handleEvent( e ):Void {
		def event = e as CollectionEvent;
		if( ExecutionManager.EXECUTIONS.equals( event.getKey() ) ) {
			def execution = event.getElement() as Execution;
			if( event.getEvent() == CollectionEvent.Event.ADDED ) {
				if( project != null and project.getAddon( ExecutionAddon.class ).getExecutions().contains( execution ) ) {
					execution.addEventListener( BaseEvent.class, executionListener );
					FxUtils.runInFxThread( function() {
						if( execution.isArchived() and pagelist.lookup( execution.getId() ) == null ) {
							insert DraggableFrame { draggable: ResultNode { execution: execution }, id: execution.getId() } before pagelist.items[0];
						}
					});
				}
			}
		}
	}
}

class ExecutionComparator extends Comparator {
	override function compare( o1, o2 ) {
		def e1 = ((o1 as DraggableFrame).draggable as ResultNode).execution;
		def e2 = ((o2 as DraggableFrame).draggable as ResultNode).execution;
		- Long.signum( e1.getStartTime() - e2.getStartTime() )
	}
}