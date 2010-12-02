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
package com.eviware.loadui.fx.statistics.chart;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.Container;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.util.Sequences;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ChartToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ComponentToolbarItem;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import java.util.EventObject;

/**
 * Base Chart Node, visualizes a ChartGroup.
 *
 * @author dain.nilsson
 */
public class ChartGroupHolder extends BaseNode, Resizable, Droppable {
	
	var title:String = "ChartGroupHolder";
	public-read var expandGroups = false;
	public-read var expandAgents = false;
	var expandedNode:Node;
	
	public-init var chartGroup:ChartGroup on replace {
		title = chartGroup.getTitle();
		chartGroup.addEventListener( BaseEvent.class, new ChartGroupListener() );
	}
	
	override var blocksMouse = true;
	
	def resizable:VBox = VBox {
		padding: Insets { left: 5, top: 5, right: 5, bottom: 5 }
		spacing: 5
		width: bind width
		height: bind height
		content: [
			Region { width: bind width, height: bind height, managed: false, style: "-fx-background-color: gray;" },
			Label { text: bind title },
			HBox {
				spacing: 5
				content: [
					Rectangle { width: 100, height: 100 },
					Rectangle { width: 400, height: 100 }
				]
			}
		]
	}
	
	def buttonBar:HBox = HBox {
		spacing: 5
		content: [
			Button { text: "Expand group", action: toggleGroupExpand, tooltip: Tooltip { text:"Hello" } },
			Button { text: "Show agents", action: toggleAgentExpand },
			Button { text: "Configure", action: toggleConfiguration }
		]
	}
	
	def configurationHolder = VBox {
	}
	
	init {
		insert buttonBar into (resizable as Container).content;
		insert configurationHolder into (resizable as Container).content;
	}
	
	override var accept = function( draggable:Draggable ):Boolean {
		draggable instanceof StatisticsToolbarItem
	}
	
	override var onDrop = function( draggable:Draggable ):Void {
		println("ChartGroupHolder.onDrop");
		if( draggable instanceof ChartToolbarItem ) {
			chartGroup.setType( (draggable as ChartToolbarItem).type );
		} else if( draggable instanceof ComponentToolbarItem ) {
			chartGroup.createChart( (draggable as ComponentToolbarItem).component );
		}
	}
	
	override function create():Node {
		resizable
	}
	
	override function getPrefHeight( width:Number ):Number {
		resizable.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ):Number {
		resizable.getPrefWidth( height )
	}
	
	public function toggleGroupExpand():Void {
		expandGroups = not expandGroups;
		if( expandGroups ) {
			if( expandAgents ) toggleAgentExpand();
			expandedNode = Rectangle { width: 500, height: 200 }
			insert expandedNode into (resizable as Container).content;
		} else {
			delete expandedNode from (resizable as Container).content;
		}
	}
	
	public function toggleAgentExpand():Void {
		expandAgents = not expandAgents;
		if( expandAgents ) {
			if( expandGroups ) toggleGroupExpand();
			expandedNode = Rectangle { width: 500, height: 300 }
			insert expandedNode into (resizable as Container).content;
		} else {
			delete expandedNode from (resizable as Container).content;
		}
	}
	
	function toggleConfiguration():Void {
		if( sizeof configurationHolder.content == 0 ) {
			insert Rectangle { height: 50, width: 500 } into configurationHolder.content;
		} else {
			configurationHolder.content = [];
		}
	}
}

class ChartGroupListener extends EventHandler {
	override function handleEvent( e:EventObject ):Void {
		def event = e as BaseEvent;
		if( ChartGroup.TITLE == event.getKey() ) {
			FxUtils.runInFxThread( function():Void {
				title = chartGroup.getTitle();
			} );
		} else if( ChartGroup.TYPE == event.getKey() ) {
			FxUtils.runInFxThread( function():Void {
			} );
		}
	}
}