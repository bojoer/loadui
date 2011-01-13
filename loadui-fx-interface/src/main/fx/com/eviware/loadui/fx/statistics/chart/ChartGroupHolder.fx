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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.util.Sequences;
import javafx.scene.control.Separator;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.util.ModelUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.dnd.SortableBox;
import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.AnalysisToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ChartToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ComponentToolbarItem;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.Chart;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import java.util.EventObject;

import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

def chartViewInfo = LayoutInfo { hfill: true, hgrow: Priority.ALWAYS }

/**
 * Base Chart Node, visualizes a ChartGroup.
 *
 * @author dain.nilsson
 */
public class ChartGroupHolder extends BaseNode, Resizable, Droppable, Releasable {
	override var styleClass = "chart-group-holder";
	
	var title:String = "ChartGroupHolder";
	var itemCount:Integer = 0;
	
	def statisticsManager = BeanInjector.getBean( StatisticsManager.class );
	
	public-read var expandGroups = false;
	public-read var expandAgents = false;
	var expandedNode:SortableBox on replace oldValue {
		for( child in oldValue.content ) ReleasableUtils.release( oldValue );
	}
	
	var chartViewHolder:ChartViewHolder on replace oldValue {
		ReleasableUtils.release( oldValue );
	}
	
	def listener = new ChartGroupListener();
	def statisticsManagerListener = new StatisticsManagerListener();
	
	def controlButtons = new ToggleGroup();
	var oldConfNode:Node;
	var oldConf:String;
	var configurationNode = bind controlButtons.selectedToggle on replace {
		var configurationNode:Node = (controlButtons.selectedToggle.value as Node);
		var selected:String = oldConf;
		if( controlButtons.selectedToggle != null )
			selected = (controlButtons.selectedToggle as ToggleButton).text;
		oldConf = selected;
		if ( selected == "Expand" ) {
				toggleGroupExpand();
		} else if ( selected == "Show agents" ) {
			toggleAgentExpand();
		} else if ( selected == "Delete" ) {
			chartGroup.delete();
		}
	};
	
	def panelToggleGroup = new PanelToggleGroup();
	def chartButtons = HBox { spacing: 5, hpos: HPos.RIGHT };
	
	public-init var chartGroup:ChartGroup on replace oldChartGroup {
		chartGroup.addEventListener( BaseEvent.class, listener );
		title = chartGroup.getTitle();
		itemCount = chartGroup.getChildCount();
		chartViewHolder = ChartViewHolder {
			chartView: chartGroup.getChartView()
			label: bind "{title} ({itemCount})"
			layoutInfo: chartViewInfo
		};
		
		rebuildChartButtons();
	}
	
	def resizable:VBox = VBox {
		padding: Insets { left: 3, top: 3, right: 3, bottom: 3 }
		spacing: 5
		width: bind width
		height: bind height
		content: [
			Region { width: bind width, height: bind height, managed: false, styleClass: "chart-group-holder" },
			//Label { text: bind "{title} ({itemCount})" },
			Stack {
				nodeHPos: HPos.LEFT
				content: bind chartViewHolder
			},
			Region { width: bind width, height: 30, layoutY: bind height - 30, managed: false, styleClass: "chart-group-holder-bottom" },
		]
	}
	
	def buttonBar:HBox = HBox {
		styleClass: "chart-group-toolbar"
		spacing: 5
		content: [
			ToggleButton { text: "Expand", toggleGroup:controlButtons, value: null },
			//Button { text: "Add Statistics", action: toggleAgentExpand },
			ToggleButton { text: "Show agents", toggleGroup:controlButtons },
			chartButtons,
			Separator { vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos:HPos.CENTER },
			ToggleButton { text: "Configure", toggleGroup:controlButtons },
		//	Button { text: "Scale", action: toggleAgentExpand }, //TODO
		//	Button { text: "Style", action: toggleAgentExpand }, //TODO
			Separator { vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos:HPos.CENTER },
		//	Button { text: "Raw Data", action: toggleAgentExpand }, //TODO
		//	Button { text: "Error", action: toggleAgentExpand }, //TODO
		//	Button { text: "Notes", action: toggleAgentExpand }, //TODO
			Separator { vertical: true, layoutInfo: LayoutInfo { height: 12 }, hpos:HPos.CENTER },
		//	Button { text: "Settings", action: toggleAgentExpand }, //TODO
		//	Button { text: "Help", action: toggleAgentExpand }, //TODO
			ToggleButton { text: "Delete", toggleGroup:controlButtons }
		]
	}
	
	def panelHolder:Stack = Stack {
		layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS }
		content: Region { managed: false, width: bind panelHolder.width, height: bind panelHolder.height, styleClass: "chart-group-panel" }
	}
	
	init {
	   statisticsManager.addEventListener( BaseEvent.class, statisticsManagerListener );
	   
		insert buttonBar into (resizable as Container).content;
		insert panelHolder into (resizable as Container).content;
	}
	
	public function update():Void {
		chartViewHolder.update();
		
		if( expandedNode != null )
			for( node in expandedNode.content )
				(node as ChartViewHolder).update();
	}
	
	public function reset():Void {
		chartViewHolder.reset();
		
		if( expandedNode != null )
			for( node in expandedNode.content )
				(node as ChartViewHolder).reset();
	}
	
	override function release():Void {
		statisticsManager.removeEventListener( BaseEvent.class, statisticsManagerListener );
		chartViewHolder = null;
		expandedNode = null;
	}
	
	override var accept = function( draggable:Draggable ):Boolean {
		draggable instanceof StatisticsToolbarItem
	}
	
	override var onDrop = function( draggable:Draggable ):Void {
		if( draggable instanceof ChartToolbarItem ) {
			chartGroup.setType( (draggable as ChartToolbarItem).type );
		} else if( draggable instanceof ComponentToolbarItem ) {
			def chart = chartGroup.createChart( (draggable as ComponentToolbarItem).component );
			def chartView = chartGroup.getChartViewForChart( chart );
			(chartView as ConfigurableLineChartView).addSegment( 'TimeTaken', 'AVERAGE', 'main' );
		} else if( draggable instanceof AnalysisToolbarItem ) {
			chartGroup.setTemplateScript( (draggable as AnalysisToolbarItem).templateScript );
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
			expandedNode = SortableBox {
				vertical: true
				layoutInfo: chartViewInfo
				content: for( chart in chartGroup.getChildren() ) ChartViewHolder {
					chartModel: chart
					chartView: chartGroup.getChartViewForChart( chart as Chart )
					label: bind ModelUtils.getLabelHolder( chart.getStatisticHolder() ).label; //"{chart.getStatisticHolder().getLabel()}"
					layoutInfo: chartViewInfo
				}
				onMoved: function( chart, fromIndex, toIndex ):Void {
					chartGroup.moveChart( (chart as ChartViewHolder).chartModel, toIndex );
				}
			}
			insert expandedNode into (resizable as Container).content;
		} else {
			for( node in expandedNode.content )
				(node as ChartViewHolder).release();
			delete expandedNode from (resizable as Container).content;
		}
	}
	
	public function toggleAgentExpand():Void {
		expandAgents = not expandAgents;
		if( expandAgents ) {
			if( expandGroups ) toggleGroupExpand();
			expandedNode = SortableBox {
				vertical: true
				layoutInfo: chartViewInfo
				content: for( source in chartGroup.getSources() ) ChartViewHolder {
					chartView: chartGroup.getChartViewForSource( source )
					label: source
					layoutInfo: chartViewInfo
				}
			}
			insert expandedNode into (resizable as Container).content;
		} else {
			for( node in expandedNode.content )
				(node as ChartViewHolder).release();
			delete expandedNode from (resizable as Container).content;
		}
	}
	
	function rebuildChartButtons() {
		chartButtons.content = for( panelFactory in ChartRegistry.getPanels( chartGroup ) ) {
			ToggleButton {
				text: panelFactory.title
				value: panelFactory
				toggleGroup: panelToggleGroup
			}
		}
	}
}

class PanelToggleGroup extends ToggleGroup {
	override var selectedToggle on replace {
		if( selectedToggle == null ) {
			for( child in panelHolder.content ) ReleasableUtils.release( child );
			panelHolder.content = panelHolder.content[0];
		} else {
			def panelFactory = selectedToggle.value as PanelFactory;
			panelHolder.content = [ panelHolder.content[0], panelFactory.build() ];
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
				rebuildChartButtons();
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