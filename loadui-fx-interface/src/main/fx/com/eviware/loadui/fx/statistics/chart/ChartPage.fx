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
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollView;
import javafx.scene.control.ScrollBarPolicy;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.util.Math;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.dnd.SortableBox;
import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.AnalysisToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ChartToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ComponentToolbarItem;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionListener;
import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.util.BeanInjector;
import java.util.EventObject;

import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;

/**
 * A Page displaying ChartGroupHolders, which allows adding, removing, and reordering of its children.
 *
 * @author dain.nilsson
 */
public class ChartPage extends BaseNode, Resizable, Releasable {
	override var styleClass = "chart-page";
	
	def listener = new ChartPageListener();
	def executionListener = new ExecutionManagerListener();
	
	def timeline = Timeline {
		repeatCount: Timeline.INDEFINITE
		keyFrames: [
			KeyFrame {
				time: 250ms
				action: function():Void {
					for( holder in innerContent )
						holder.update();
				}
			}
		]
	}
	
	def executionManager = BeanInjector.getBean( ExecutionManager.class ) on replace {
		executionManager.addExecutionListener( executionListener );
		timeline.playFromStart();
	}
	
	public-init var statisticPage:StatisticPage on replace oldValue {
		if( oldValue != null )
			oldValue.removeEventListener( BaseEvent.class, listener );
		if( statisticPage != null ) {
			statisticPage.addEventListener( BaseEvent.class, listener );
			innerContent = for( chartGroup in statisticPage.getChildren() ) ChartGroupHolder {
				chartGroup: chartGroup as ChartGroup
				layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.SOMETIMES }
			};
		}
	}
	
	var innerContent:ChartGroupHolder[] on replace {
		container.content = innerContent;
	}
	
	override var layoutInfo = LayoutInfo {
		hfill: true
		vfill: true
		hgrow: Priority.ALWAYS
		vgrow: Priority.ALWAYS
	}
	
	var container:SortableBox;
	def resizable:ScrollView = ScrollView {
		width: bind width
		height: bind height
		hbarPolicy: ScrollBarPolicy.NEVER
		vbarPolicy: ScrollBarPolicy.ALWAYS
		fitToWidth: true
		styleClass: "chart-page-scroll-view"
		node: Stack {
			nodeVPos: VPos.TOP
			content: [
				DropBase {
					layoutInfo: LayoutInfo { height: bind Math.max( height, container.height ), width: bind width }
				}, container = SortableBox {
					vertical: true
					spacing: 5
					enforceBounds: false
					padding: Insets { top: 5, right: 5, bottom: 25 }
					layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, vgrow: Priority.NEVER, vfill: false }
					content: innerContent
					onMoved: function( child, fromIndex, toIndex ):Void {
						statisticPage.moveChartGroup( (child as ChartGroupHolder).chartGroup, toIndex );
					}
				}, Rectangle {
					width: bind container.width
					height: bind container.height
					fill: Color.TRANSPARENT
					onMouseWheelMoved: function( e ) {
						def stepSize = (resizable.vmax - resizable.vmin) * (50.0 / (container.height - height));
						resizable.vvalue = Math.max( resizable.vmin, Math.min( resizable.vmax, resizable.vvalue + stepSize*e.wheelRotation ) );
					}
				}
			]
		}
	}
	
	override function release():Void {
		timeline.stop();
		executionManager.removeExecutionListener( executionListener );
		statisticPage = null;
		for( holder in innerContent )
			holder.release();
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
}

class ExecutionManagerListener extends ExecutionListenerAdapter {
	var resetOnStart = false;
	
   override function executionStarted(state:ExecutionManager.State) {
		if( resetOnStart ) {
   		for( holder in innerContent )
   			holder.reset();
   		resetOnStart = false;
   	}
   }

	override function executionPaused(state:ExecutionManager.State) {
	}

	override function executionStopped(state:ExecutionManager.State) {
		resetOnStart = true;
	}
}

class ChartPageListener extends EventHandler {
	override function handleEvent( e:EventObject ):Void {
		def event = e as BaseEvent;
		if( StatisticPage.CHILDREN == event.getKey() ) {
			def cEvent = event as CollectionEvent;
			FxUtils.runInFxThread( function():Void {
				if( cEvent.getEvent() == CollectionEvent.Event.ADDED ) {
					insert ChartGroupHolder {
						chartGroup: cEvent.getElement() as ChartGroup
						layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.SOMETIMES }
					} into innerContent;
				} else {
					for( chartGroupHolder in innerContent [x|x.chartGroup == cEvent.getElement()] ) {
						delete chartGroupHolder from innerContent;
						chartGroupHolder.release();
					}
				}
			} );
		}
	}
}

class DropBase extends BaseNode, Resizable, Droppable {
	def dropBaseNode = Region {
		styleClass: "chart-drop-base"
		width: bind width
		height: bind height
	}
	
	override var accept = function( draggable:Draggable ):Boolean {
		draggable instanceof StatisticsToolbarItem
	}
	
	override var onDrop = function( draggable:Draggable ):Void {
		if( draggable instanceof ChartToolbarItem ) {
			statisticPage.createChartGroup( (draggable as ChartToolbarItem).type, "Chart Group {statisticPage.getChildCount()+1}" )
		} else if( draggable instanceof ComponentToolbarItem ) {
			def sh = (draggable as ComponentToolbarItem).component;
			def chartGroup = statisticPage.createChartGroup( com.eviware.loadui.api.statistics.model.chart.LineChartView.class.getName(), "Chart Group {statisticPage.getChildCount()+1}" );
			def chart = chartGroup.createChart( sh );
			def chartView = chartGroup.getChartViewForChart( chart );
			(chartView as ConfigurableLineChartView).addSegment( 'TimeTaken', 'AVERAGE', 'main' );
		} else if( draggable instanceof AnalysisToolbarItem ) {
			def chartGroup = statisticPage.createChartGroup( com.eviware.loadui.api.statistics.model.chart.LineChartView.class.getName(), "Chart Group {statisticPage.getChildCount()+1}" );
			chartGroup.setTemplateScript( (draggable as AnalysisToolbarItem).templateScript );
		}
	}
	
	override function create():Node {
		dropBaseNode
	}
	
	override function getPrefHeight( width:Number ):Number {
		dropBaseNode.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ):Number {
		dropBaseNode.getPrefWidth( height )
	}
}