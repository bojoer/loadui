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
import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.util.Sequences;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ChartToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ComponentToolbarItem;

import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionListener;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.util.BeanInjector;
import java.util.EventObject;

def chartViewInfo = LayoutInfo { hfill: true, hgrow: Priority.ALWAYS }

/**
 * Base Chart Node, visualizes a ChartGroup.
 *
 * @author dain.nilsson
 */
public class ChartGroupHolder extends BaseNode, Resizable, Droppable {
	var title:String = "ChartGroupHolder";
	var itemCount:Integer = 0;
	
	def statisticsManager = BeanInjector.getBean(StatisticsManager.class);
	def executionManager = BeanInjector.getBean(ExecutionManager.class);
	
	public-read var expandGroups = false;
	public-read var expandAgents = false;
	var expandedNode:Node;
	
	var chartViewHolder:ChartViewHolder;
	
	def listener = new ChartGroupListener();
	def statisticsManagerListener = new StatisticsManagerListener();
	def executionManagerListener = new ExecutionManagerListener();
	
	public-init var chartGroup:ChartGroup on replace oldChartGroup {
		chartGroup.addEventListener( BaseEvent.class, listener );
		title = chartGroup.getTitle();
		itemCount = chartGroup.getChildCount();
		chartViewHolder = ChartViewHolder {
			chartView: chartGroup.getChartView()
			label: bind "{title} ({itemCount})"
			layoutInfo: chartViewInfo
		};
	}
	
	override var blocksMouse = true;
	
	def resizable:VBox = VBox {
		padding: Insets { left: 5, top: 5, right: 5, bottom: 5 }
		spacing: 5
		width: bind width
		height: bind height
		content: [
			Region { width: bind width, height: bind height, managed: false, style: "-fx-background-color: gray;" },
			//Label { text: bind "{title} ({itemCount})" },
			Stack { nodeHPos: HPos.LEFT,  content: bind chartViewHolder }
		]
	}
	
	def buttonBar:HBox = HBox {
		spacing: 5
		content: [
			Button { text: "Expand group", action: toggleGroupExpand },
			Button { text: "Show agents", action: toggleAgentExpand },
			Button { text: "Configure", action: toggleConfiguration },
			Button { text: "Delete", action: function():Void { chartGroup.delete() } }
		]
	}
	
	def configurationHolder = VBox {
	}
	
	init {
	   statisticsManager.addEventListener( BaseEvent.class, statisticsManagerListener );
	   executionManager.addExecutionListener( executionManagerListener );
	   
		insert buttonBar into (resizable as Container).content;
		insert configurationHolder into (resizable as Container).content;
	}
	
	override var accept = function( draggable:Draggable ):Boolean {
		draggable instanceof StatisticsToolbarItem
	}
	
	override var onDrop = function( draggable:Draggable ):Void {
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
			expandedNode = VBox {
				content: for( chart in chartGroup.getChildren() ) ChartViewHolder {
					chartView: chartGroup.getChartViewForChart( chart as Chart )
					label: "{chart.getStatisticHolder()}"
					layoutInfo: chartViewInfo
				}
			}
			insert expandedNode into (resizable as Container).content;
		} else {
			delete expandedNode from (resizable as Container).content;
		}
	}
	
	public function toggleAgentExpand():Void {
		expandAgents = not expandAgents;
		if( expandAgents ) {
			if( expandGroups ) toggleGroupExpand();
			expandedNode = VBox {
				content: for( source in chartGroup.getSources() ) ChartViewHolder {
					chartView: chartGroup.getChartViewForSource( source )
					label: source
					layoutInfo: chartViewInfo
				}
			}
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
				chartViewHolder = ChartViewHolder {
					chartView: chartGroup.getChartView()
					label: bind "{title} ({itemCount})"
					layoutInfo: chartViewInfo
				};
				if( expandGroups ) {
					toggleGroupExpand();
					toggleGroupExpand();
				} else if( expandAgents ) {
					toggleAgentExpand();
					toggleAgentExpand();
				}
			} );
		} else if( ChartGroup.CHILDREN == event.getKey() ) {
			FxUtils.runInFxThread( function():Void {
				itemCount = chartGroup.getChildCount();
				if( expandGroups ) {
					toggleGroupExpand();
					toggleGroupExpand();
				} else if( expandAgents ) {
					toggleAgentExpand();
					toggleAgentExpand();
				}
			} );
		}
	}
}

class StatisticsManagerListener extends EventHandler {  
    override function handleEvent(e: EventObject) { 
		if( e instanceof CollectionEvent ) {
			def event: CollectionEvent = e as CollectionEvent;
			if(event.getSource() instanceof StatisticsManager and event.getElement() instanceof StatisticHolder){
				if(event.getEvent() == CollectionEvent.Event.REMOVED){
					FxUtils.runInFxThread( function(): Void {
					   def sh: StatisticHolder = event.getElement() as StatisticHolder;
					   for(chart in chartGroup.getChildren()){
					      if(chart.getStatisticHolder() == sh){
					         chart.delete();
					         break;
					      }
					   }
					   if(chartGroup.getChildCount() == 0){
					       chartGroup.delete();
					   }
					});
				}
			}
		}
	}
}

class ExecutionManagerListener extends ExecutionListener {  

   override function executionStarted(state:ExecutionManager.State){
   	println("---satrted");
   }

	override function executionPaused(state:ExecutionManager.State){
	   println("---paused"); 
	}

	override function executionStoped(state:ExecutionManager.State){
	    println("---stoped");
	}
}