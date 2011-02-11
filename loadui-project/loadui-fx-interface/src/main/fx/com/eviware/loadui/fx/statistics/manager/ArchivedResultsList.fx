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

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.pagelist.PageList;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.util.BeanInjector;

public class ArchivedResultsList extends BaseNode, Resizable, Droppable {
	def pagelist = PageList { width: bind width, height: bind height, label: "Archived Results" };
	
	def manager:ExecutionManager = BeanInjector.getBean( ExecutionManager.class ) on replace {
		pagelist.items = for( name in manager.getExecutionNames() ) DraggableFrame { draggable: ResultNode { execution: manager.getExecution( name ) } }
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
	
	override var accept = function( d:Draggable ) {
		d.node instanceof ResultNode /*and not (d.node as ResultNode).execution.isArchived()*/
	}
	
	override var onDrop = function( d:Draggable ) {
		if( d.node instanceof ResultNode ) {
			//(d.node as ResultNode).execution.archive()
		}
	}
}

class ArchiveListener extends EventHandler {
	override function handleEvent( e ):Void {
	}
}