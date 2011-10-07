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
package com.eviware.loadui.fx.statistics.chart;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
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
import com.eviware.loadui.fx.widgets.canvas.Selectable;

import com.eviware.loadui.api.traits.Releasable;;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.CanvasItem;
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
import java.util.ArrayList;
import java.util.Collections;

import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

def EXPAND_ATTRIBUTE = "expand";
def GROUP = "group";
def SOURCES = "sources";
def NONE = "none";

def chartViewInfo = LayoutInfo { hfill: true, hgrow: Priority.ALWAYS };
def childrenInfo = LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, margin: Insets { left: 8, right: 8, top: -1, bottom: 8 } };

/**
 * Base Chart Node, visualizes a ChartGroup.
 *
 * @author dain.nilsson
 */
public class ChartGroupHolder extends BaseNode, Resizable, Releasable, Deletable, Droppable, Selectable {
	override var styleClass = "chart-group-holder";
	
	var title:String = "ChartGroupHolder";
	var chartGroupHolder = this;
	
	def statisticsManagerListener = new StatisticsManagerListener();
	def statisticsManager = BeanInjector.getBean( StatisticsManager.class ) on replace {
		statisticsManager.addEventListener( BaseEvent.class, statisticsManagerListener );
	}
	
	public-read var expandGroups = false;
	public-read var expandAgents = false;
	var expandedNode:SortableBox on replace oldValue {
		for( child in oldValue.content ) ReleasableUtils.release( child );
		delete oldValue from (resizable as Container).content;
		
		expandedChartViews = for( node in expandedNode.content[x|x instanceof ChartViewHolder] ) node as ChartViewHolder;
	}
	
	public-read var expandedChartViews:ChartViewHolder[];
	
	public-read var chartViewHolder:ChartViewHolder on replace oldValue {
		ReleasableUtils.release( oldValue );
	}
	
	def listener = new ChartGroupListener();
	
	def graphic = Group {};
	
	public-init var chartGroup:ChartGroup on replace oldChartGroup {
		chartGroup.addEventListener( BaseEvent.class, listener );
		title = chartGroup.getLabel();
		refreshGraphic();
		chartViewHolder = ChartGroupChartViewHolder {
			chartGroupHolder: chartGroupHolder
			chartGroup: chartGroup
			typeLabel: "Component"
			label: bind title
			layoutInfo: chartViewInfo
			graphic: graphic
		};
		def expandState = chartGroup.getAttribute( EXPAND_ATTRIBUTE, NONE );
		if( GROUP.equals( expandState ) and not expandGroups ) {
			toggleGroupExpand();
		} else if( SOURCES.equals( expandState ) and not expandAgents ) {
			toggleAgentExpand();
		} else {
			if( expandGroups )
				toggleGroupExpand();
			if( expandAgents )
				toggleAgentExpand();
		}
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
			Region {
				styleClass: bind if( selected ) "chart-group-holder-selected" else "chart-group-holder",
				managed: false,
				height: bind height,
				width: bind width
			}
			groupContent = VBox {
				content: [
					Region { width: bind groupContent.width, height: bind groupContent.height, managed: false, styleClass: "chart-group-face" },
					Stack {
						nodeHPos: HPos.LEFT
						content: bind chartViewHolder
					}
				]
			},
			panelHolder,
			expandedNode
		]
	}
	
	init {
		addMouseHandler( MOUSE_PRESSED, function( e:MouseEvent ) {
			if( e.controlDown ) { if( selected ) deselect() else select() } else if( not selected ) selectOnly();
		} );
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
			ChartDefaults.createSubChart( chartGroup, sh );
		} else if( draggable instanceof AnalysisToolbarItem ) {
			chartGroup.setTemplateScript( (draggable as AnalysisToolbarItem).templateScript );
			//Just apply the script once, don't keep it attached.
			chartGroup.setTemplateScript( null );
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
				content: for( chart in chartGroup.getChildren() ) {
					def subChartView = chartGroup.getChartViewForChart( chart as Chart );
					if( not "true".equals( subChartView.getAttribute( "saved", "false" ) ) ) {
						subChartView.setAttribute( "saved", "true" );
						subChartView.setAttribute( "position", null );
						subChartView.setAttribute( "timeSpan", null );
						subChartView.setAttribute( "zoomLevel", null );
					}
					ChartViewHolder {
						chartModel: chart
						chartView: subChartView
						label: bind ModelUtils.getLabelHolder( chart.getStatisticHolder() ).label;
						layoutInfo: chartViewInfo
						graphic: ImageView { image: FxUtils.getImageFor( chart.getStatisticHolder() ) }
					}
				}
				onMoved: function( chart, fromIndex, toIndex ):Void {
					chartGroup.moveChart( (chart as ChartViewHolder).chartModel, toIndex );
				}
			}
			insert expandedNode into (resizable as Container).content;
		} else {
			expandedNode = null;
		}
		chartGroup.setAttribute( EXPAND_ATTRIBUTE, if( expandGroups ) GROUP else NONE );
	}
	
	public function toggleAgentExpand():Void {
		expandAgents = not expandAgents;
		if( expandAgents ) {
			if( expandGroups ) toggleGroupExpand();
			def sources = new ArrayList( chartGroup.getSources() );
			Collections.sort( sources );
			expandedNode = SortableBox {
				vertical: true
				layoutInfo: childrenInfo
				spacing: 5
				content: for( source in sources ) {
					def subChartView = chartGroup.getChartViewForSource( source as String );
					if( not "true".equals( subChartView.getAttribute( "saved", "false" ) ) ) {
						subChartView.setAttribute( "saved", "true" );
						subChartView.setAttribute( "position", null );
						subChartView.setAttribute( "timeSpan", null );
						subChartView.setAttribute( "zoomLevel", null );
					}
					ChartViewHolder {
						chartView: subChartView
						label: source as String
						typeLabel: "Agent"
						layoutInfo: chartViewInfo
						graphic: ImageView { image: FxUtils.agentImage }
					}
				}
			}
			insert expandedNode into (resizable as Container).content;
		} else {
			expandedNode = null;
		}
		chartGroup.setAttribute( EXPAND_ATTRIBUTE, if( expandAgents ) SOURCES else NONE );
	}
	
	function refreshGraphic():Void {
		var offset = 0;
		graphic.content = Sequences.reverse( for( chart in chartGroup.getChildren() ) {
			offset += 5;
			ImageView { image: FxUtils.getImageFor( chart.getStatisticHolder() ), layoutX: offset, layoutY: offset }
		} ) as Node[];
	}
}

class ChartGroupListener extends EventHandler {
	override function handleEvent( e:EventObject ):Void {
		def event = e as BaseEvent;
		if( ChartGroup.LABEL == event.getKey() ) {
			FxUtils.runInFxThread( function():Void {
				title = chartGroup.getLabel();
			} );
		} else if( ChartGroup.TYPE == event.getKey() ) {
			FxUtils.runInFxThread( function():Void {
				refreshGraphic();
				chartViewHolder = ChartGroupChartViewHolder {
					chartGroupHolder: chartGroupHolder
					chartGroup: chartGroup
					layoutInfo: chartViewInfo
					graphic: graphic
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
				refreshGraphic();
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
					});
				}
			}
		}
	}
}