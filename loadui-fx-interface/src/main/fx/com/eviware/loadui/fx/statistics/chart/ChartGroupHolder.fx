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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.util.Sequences;
import javafx.scene.control.Separator;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.FxUtils.__ROOT__;
import com.eviware.loadui.fx.util.ModelUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.dnd.SortableBox;
import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.AnalysisToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ChartToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.StatisticHolderToolbarItem;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.Chart;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.BeanInjector;
import java.util.EventObject;

import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

def chartViewInfo = LayoutInfo { hfill: true, hgrow: Priority.ALWAYS };
def childrenInfo = LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, margin: Insets { left: 8, right: 8, top: -1, bottom: 8 } };

/**
 * Base Chart Node, visualizes a ChartGroup.
 *
 * @author dain.nilsson
 */
public class ChartGroupHolder extends BaseNode, Resizable, Releasable, Deletable, Droppable {
	override var styleClass = "chart-group-holder";
	
	var title:String = "ChartGroupHolder";
	var itemCount:Integer = 0;
	var chartGroupHolder = this;
	
	def statisticsManagerListener = new StatisticsManagerListener();
	def statisticsManager = BeanInjector.getBean( StatisticsManager.class ) on replace {
		statisticsManager.addEventListener( BaseEvent.class, statisticsManagerListener );
	}
	
	public-read var expandGroups = false;
	public-read var expandAgents = false;
	var expandedNode:SortableBox on replace oldValue {
		for( child in oldValue.content ) ReleasableUtils.release( oldValue );
	}
	
	var chartViewHolder:ChartViewHolder on replace oldValue {
		ReleasableUtils.release( oldValue );
	}
	
	def listener = new ChartGroupListener();
	
	public-init var chartGroup:ChartGroup on replace oldChartGroup {
		chartGroup.addEventListener( BaseEvent.class, listener );
		title = chartGroup.getTitle();
		itemCount = chartGroup.getChildCount();
		chartViewHolder = ChartGroupChartViewHolder {
			chartGroupHolder: chartGroupHolder
			chartGroup: chartGroup
			typeLabel: bind "Component ({itemCount})"
			label: bind title
			layoutInfo: chartViewInfo
		};
	}
	
	def panelHolder:Stack = Stack {
		styleClass: "chart-group-panel"
		padding: Insets { top: 17, right: 17, bottom: 17, left: 17 }
		layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, margin: Insets { top: -7 } }
		content: Region { managed: false, width: bind panelHolder.width, height: bind panelHolder.height, styleClass: "chart-group-panel" }
		visible: bind ( sizeof panelHolder.content > 1 )
		managed: bind ( sizeof panelHolder.content > 1 )
	}
	
	var groupContent:VBox;
	def resizable:VBox = VBox {
		spacing: 6
		width: bind width
		height: bind height
		content: [
			Region { styleClass: "chart-group-holder", managed: false, height: bind height, width: bind width }
			groupContent = VBox {
				content: [
					Region { width: bind groupContent.width, height: bind groupContent.height, managed: false, styleClass: "chart-group-face" },
					Stack {
						nodeHPos: HPos.LEFT
						content: bind chartViewHolder
					}
				]
			},
			panelHolder
		]
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
	
	override var accept = function( draggable:Draggable ):Boolean {
		draggable instanceof StatisticsToolbarItem
	}
	
	override var onDrop = function( draggable:Draggable ):Void {
		if( draggable instanceof ChartToolbarItem ) {
			chartGroup.setType( (draggable as ChartToolbarItem).type );
		} else if( draggable instanceof StatisticHolderToolbarItem ) {
			def sh = (draggable as StatisticHolderToolbarItem).statisticHolder;
			def chart = chartGroup.createChart( sh );
			def variable = sh.getStatisticVariable( "TimeTaken" );
			if( variable != null and variable.getStatisticNames().contains( "AVERAGE" ) ) {
				def chartView = chartGroup.getChartViewForChart( chart );
				(chartView as ConfigurableLineChartView).addSegment( 'TimeTaken', 'AVERAGE', StatisticVariable.MAIN_SOURCE );
			}
		} else if( draggable instanceof AnalysisToolbarItem ) {
			chartGroup.setTemplateScript( (draggable as AnalysisToolbarItem).templateScript );
		}
	}
	
	override function release():Void {
		statisticsManager.removeEventListener( BaseEvent.class, statisticsManagerListener );
		chartViewHolder = null;
		expandedNode = null;
	}
	
	override function doDelete():Void {
		chartGroup.delete();
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
				enforceBounds: false
				vertical: true
				layoutInfo: childrenInfo
				spacing: 5
				content: for( chart in chartGroup.getChildren() ) ChartViewHolder {
					chartModel: chart
					chartView: chartGroup.getChartViewForChart( chart as Chart )
					label: bind ModelUtils.getLabelHolder( chart.getStatisticHolder() ).label;
					layoutInfo: chartViewInfo
					graphic: ImageView { image: FxUtils.getImageFor( chart.getStatisticHolder() ) }
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
				layoutInfo: childrenInfo
				spacing: 5
				content: for( source in chartGroup.getSources() ) ChartViewHolder {
					chartView: chartGroup.getChartViewForSource( source )
					label: source
					typeLabel: "Agent"
					layoutInfo: chartViewInfo
					graphic: ImageView { image: FxUtils.agentImage }
				}
			}
			insert expandedNode into (resizable as Container).content;
		} else {
			for( node in expandedNode.content )
				(node as ChartViewHolder).release();
			delete expandedNode from (resizable as Container).content;
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
				chartViewHolder = ChartGroupChartViewHolder {
					chartGroupHolder: chartGroupHolder
					chartGroup: chartGroup
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