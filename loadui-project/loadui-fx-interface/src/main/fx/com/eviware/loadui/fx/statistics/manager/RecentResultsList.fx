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
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.pagelist.PageList;

import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.statistics.ProjectExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.util.BeanInjector;

public class RecentResultsList extends BaseNode, Resizable {
	def pagelist = PageList { width: bind width, height: bind height, label: "Recent Results" };
	def listener = new ExecutionsListener();
	
	def projectExecutionManager:ProjectExecutionManager = BeanInjector.getBean( ProjectExecutionManager.class );
	
	def manager:ExecutionManager = BeanInjector.getBean( ExecutionManager.class ) on replace {
		manager.addEventListener( CollectionEvent.class, listener );
	}
	
	def project = bind StatisticsWindow.instance.project on replace {
		if( project != null ) {
			println("Getting executions for project: {project}");
			for( execution in projectExecutionManager.getExecutions( project, true, false ) ) {
				if( pagelist.lookup( execution.getId() ) == null ) {
					insert DraggableFrame { draggable: ResultNode { execution: execution }, id: execution.getId() } before pagelist.items[0];
				}
			}
		}
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

class ExecutionsListener extends WeakEventHandler {
	override function handleEvent( e ):Void {
		def event = e as CollectionEvent;
		if( ExecutionManager.EXECUTIONS.equals( event.getKey() ) ) {
			if( event.getEvent() == CollectionEvent.Event.ADDED ) {
				FxUtils.runInFxThread( function() {
					def execution = event.getElement() as Execution;
					if( project != null and project.getId() == projectExecutionManager.getProjectId( execution ) 
							and not execution.isArchived() and pagelist.lookup( execution.getId() ) == null ) {
						insert DraggableFrame { draggable: ResultNode { execution: execution }, id: execution.getId() } before pagelist.items[0];
					}
				} );
			} else {
				FxUtils.runInFxThread( function() {
					def execution = event.getElement() as Execution;
					delete pagelist.lookup( execution.getId() ) from pagelist.items;
				} );
			}
		}
	}
}