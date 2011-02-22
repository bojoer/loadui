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
package com.eviware.loadui.fx.statistics.chart.line;

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
import javafx.scene.control.ScrollBar;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.util.Math;


import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.treeselector.CascadingTreeSelector;
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.fx.statistics.chart.BaseChart;
import com.eviware.loadui.fx.statistics.chart.SegmentTreeModel;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.util.StringUtils;
import java.awt.BasicStroke;
import java.beans.PropertyChangeEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.lang.Runnable;

import javafx.ext.swing.SwingComponent;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.axis.NumericAxis;
import com.jidesoft.chart.axis.TimeAxis;
import com.jidesoft.chart.style.ChartStyle;

public function getLineSegmentChartModel( lineSegment:LineSegment ):LineSegmentChartModel {
	for( chart in chartSet ) {
		for( model in (chart as LineChart).lines.values() ) {
			def segmentModel = model as LineSegmentChartModel;
			if( segmentModel.segment == lineSegment )
				return segmentModel
		}
	}
	
	return null;
}

def chartSet = new HashSet();

/**
 * Base LineChart Node, visualizes a LineChartView.
 * 
 * @author dain.nilsson
*/
public class LineChart extends BaseNode, Resizable, BaseChart, Releasable {
	override var styleClass = "line-chart";
	
	def xRange = new LongRange( 0, 0 );
	def listener = new ChartViewListener();
	def lines = new HashMap();
	def comparedLines = new HashMap();
	public-read def chart = new Chart();
	def chartNode = SwingComponent.wrap( chart );
	def timeCalculator = new TotalTimeTickCalculator();
	var compactSegments = true;
	var zoomLevel = 0;
	
	def execution = bind StatisticsWindow.execution on replace {
		reset();
		update();
	}
	
	def comparedExecution = bind StatisticsWindow.comparedExecution on replace {
		for( model in chart.getModels()[x|x instanceof ComparedLineSegmentChartModel] )
			chart.removeModel( model );
		comparedLines.clear();
		
		if( comparedExecution != null ) {
			for( model in chart.getModels()[x|x instanceof LineSegmentChartModel] ) {
				def comparedModel = ComparedLineSegmentChartModel { baseModel: model as LineSegmentChartModel };
				comparedLines.put( model, comparedModel );
				chart.addModel( comparedModel, comparedModel.chartStyle );
			}
		}
		
		reset();
		update();
	}
	
	def scrollBar = ScrollBar {
		vertical: false
		layoutInfo: LayoutInfo { hgrow: Priority.ALWAYS, hfill: true, margin: Insets { top: - 1, right: - 1, bottom: - 1, left: - 1 } }
		clickToPosition: true
	}
	def scrollBarPosition = bind scrollBar.value on replace {
		position = scrollBarPosition * ( maxTime - timeSpan ) / maxTime;
		xRange.setMin( position );
		xRange.setMax( position + timeSpan );
	}
	
	var padding = 2;
	var min:Number = 0;
	var max:Number = 0;
	var maxTime:Number = 0;
	var position:Number = 0;
	var showAll = false on replace {
		if( showAll ) timeSpan = maxTime as Integer;
	}
	var timeSpan:Long = 10000 on replace oldTimeSpan {
		scrollBar.visibleAmount = timeSpan;
		scrollBar.unitIncrement = timeSpan / 10;
		scrollBar.blockIncrement = timeSpan / 10;
		
		if( oldTimeSpan > maxTime and timeSpan < maxTime )
			scrollBar.value = maxTime;
	}
	def chartNodeWidth = bind chartNode.width - 20 on replace {
		if( not showAll )
			timeSpan = (chartNodeWidth * xScale) as Long;
	} 
	var xScale:Number = 0.002 on replace {
		timeSpan = (chartNodeWidth * xScale) as Long;
	}
	
	def segmentButtons:VBox = VBox {
		layoutInfo: LayoutInfo { hgrow: Priority.NEVER, hfill: false }, 
		padding: Insets { top: 8, right: 8, bottom: 8, left: 8 }
		spacing: 4
		content: [
			Region { managed: false, width: bind segmentButtons.width, height: bind segmentButtons.height, styleClass: "chart-view-panel" },
			HBox {
				layoutInfo: LayoutInfo { vfill: false, vgrow: Priority.NEVER }
				content: [
					Label { text: "Statistic", layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS } },
					Button {
						styleClass: "compact-panel-button"
						graphic: SVGPath {
							fill: Color.rgb( 0xb2, 0xb2, 0xb2 )
							content: bind if(compactSegments) "M 0 0 L 3.5 3.5 0 7 0 0 M 3.5 0 L 7 3.5 3.5 7 3.5 0" else "M 0 0 L -3.5 3.5 0 7 0 0 M -3.5 0 L -7 3.5 -3.5 7 -3.5 0"
						}
						action: function():Void { compactSegments = not compactSegments }
					}
				]
			}
		]
	}
	
	public-init var chartView:LineChartView on replace oldChartView {
		if( chartView != null ) {
			chartView.addEventListener( EventObject.class, listener );
			setZoomLevel( chartView.getAttribute( ZoomPanel.ZOOM_LEVEL_ATTRIBUTE, ZoomPanel.ZOOM_DEFAULT ) );
			
			for( segment in chartView.getSegments() )
				addedSegment( segment );
		}
		
		if( oldChartView != null ) {
			oldChartView.removeEventListener( EventObject.class, listener );
			lines.clear();
		}
	}
	
	override var layoutInfo = LayoutInfo { vfill: true, hfill: true, vgrow: Priority.ALWAYS, hgrow: Priority.ALWAYS, minWidth: 200 }
	
	var chartVbox:VBox;
	def resizable:VBox = VBox {
		width: bind width
		height: bind height
		spacing: 5
		content: [
			HBox {
				padding: Insets { left: -3, right: 15 }
				spacing: 10
				content: [
					Label { layoutInfo: LayoutInfo { height: 163, margin: Insets { right: -10 } }, visible: false },
					segmentButtons,
					chartVbox = VBox {
						padding: Insets { top: 5 }
						content: [
							Region { styleClass: "base-chart", managed: false, width: bind chartVbox.width, height: bind chartVbox.height }, 
							chartNode,
							SVGPath {
								managed: false
								clip: Rectangle { width: bind chartVbox.width, height: bind chartVbox.height }
								fill: LinearGradient {
									startX: 0.0
									startY: 0.0
									endX: 30.0
									endY: 30.0
									proportional: false
									stops: [
										Stop { offset: 0.0 color: Color.rgb( 0xff, 0xff, 0xff, 0.2 ) },
										Stop { offset: 1.0 color: Color.rgb( 0xff, 0xff, 0xff, 0.06 ) }
									]
								}
								content: "M3.093,0C-0.12,0,0,0.108,0,2.651v98.056c0,1.465,1.385,2.651,3.093,2.651h1.051C16.495,81.713,21.385,0.008,297.475,0.008L3.093,0z"
							},
							scrollBar
						]
					}
				]
			}
		]
	}
	
	init {
		chartSet.add( this );
		LineChartStyles.styleChart( chart );
		
		def xAxis = new TimeAxis();
		chart.setXAxis( xAxis );
		xRange.setMin( 0 );
		xRange.setMax( timeSpan );
		xAxis.setRange( xRange );
		xAxis.setTickCalculator( timeCalculator );
		
		def yAxis = chart.getYAxis();
		yAxis.setRange( 0, 10 );
		yAxis.setLabelVisible( false );
		
		chartNode.layoutInfo = LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, vfill: true, vgrow: Priority.ALWAYS, margin: Insets { left: -15, right: 10 } };
	}
	
	override function update():Void {
		maxTime = execution.getLength();
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		def shouldPoll = StatisticsWindow.execution == StatisticsWindow.currentExecution;
		for( m in lines.values() ) {
			def model = m as LineSegmentChartModel;
			if( shouldPoll )
				model.poll();
				
			maxTime = Math.max( maxTime, (model as LineSegmentChartModel).latestTime );
			def yRange = model.getYRange( 0.05, 0.05 );
			if( yRange.minimum() != Double.NEGATIVE_INFINITY and yRange.maximum() != Double.POSITIVE_INFINITY ) {
				min = Math.min( min, yRange.minimum() );
				max = Math.max( max, yRange.maximum() );
			}
		}
		if( comparedExecution != null ) {
			maxTime = Math.max( maxTime, comparedExecution.getLength() );
			for( m in comparedLines.values() ) {
				def model = m as ComparedLineSegmentChartModel;
					
				def yRange = model.getYRange( 0.05, 0.05 );
				if( yRange.minimum() != Double.NEGATIVE_INFINITY and yRange.maximum() != Double.POSITIVE_INFINITY ) {
					min = Math.min( min, yRange.minimum() );
					max = Math.max( max, yRange.maximum() );
				}
			}
		}
		if( min > max ) {
			min = 0;
			max = 100;
		} else if( min == max ) {
			min -= 2.5;
			max += 2.5;
		}
		chart.getYAxis().setRange( min, max );
		if( showAll ) {
			timeSpan = maxTime as Integer;
			def level = TotalTimeTickCalculator.Level.forSpan( timeSpan / 1000 );
			zoomLevel = level.getLevel();
		}
		
		if( maxTime > timeSpan ) {
			if( scrollBar.max < timeSpan or scrollBar.value == scrollBar.max ) {
				scrollBar.max = maxTime;
				scrollBar.value = maxTime;
				position = maxTime - timeSpan;
			} else {
				scrollBar.max = maxTime;
			}
		} else {
			scrollBar.max = 0;
			position = 0;
		}

		def end = position + timeSpan;
		xRange.setMin( position );
		xRange.setMax( end );
		
		def padding = 10000;
		for( m in lines.values() ) {
			def model = m as LineSegmentChartModel;
			model.xRange = [ position - padding, end + padding ];
		}
	}
	
	override function reset():Void {
		def fixedTime = StatisticsWindow.execution != StatisticsWindow.currentExecution;
		for( m in lines.values() ) {
			def model = m as LineSegmentChartModel;
			model.xRange = [ 0, 0 ];
			//model.clearPoints();
		}
			
		maxTime = 0;
		max = 100;
		min = 0;
	}
	
	override function release():Void {
		chartView = null;
		chartSet.remove( this );
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
	
	function addedSegment( segment:LineSegment ):Void {
		def model = LineSegmentChartModel { chartView: chartView, segment: segment, level: bind zoomLevel };
		lines.put( segment, model );
		chart.addModel( model, model.chartStyle );
		if( comparedExecution != null ) {
			def comparedModel = ComparedLineSegmentChartModel { baseModel: model };
			comparedLines.put( model, comparedModel );
			chart.addModel( comparedModel, comparedModel.chartStyle );
		}
		insert if( chartView instanceof ConfigurableLineChartView ) {
			DraggableFrame { draggable: DeletableSegmentButton { compactSegments: bind compactSegments, chartView: chartView, model: model, confirmDialogScene: bind scene } };
		} else {
			SegmentButton { compactSegments: bind compactSegments, chartView: chartView, model: model };
		} into segmentButtons.content;
	}
	
	function removedSegment( segment:LineSegment ):Void {
		def model = lines.remove( segment ) as LineSegmentChartModel;
		if( model != null ) {
			chart.removeModel( model );
			def comparedModel = lines.remove( model ) as LineSegmentChartModel;
			if( comparedModel != null ) {
				chart.removeModel( comparedModel );
			}
		}
		for( frame in segmentButtons.content[b | b instanceof DraggableFrame] ) {
			def button = (frame as DraggableFrame).draggable as DeletableSegmentButton;
			if( button.model.segment == segment )
				delete frame from segmentButtons.content;
		}
		for( button in segmentButtons.content[b | b instanceof SegmentButton] ) {
			if( (button as SegmentButton).model.segment == segment )
				delete button from segmentButtons.content;
		}
	}
	
	public function setZoomLevel( level:String ):Void {
		def newLevel = TotalTimeTickCalculator.Level.valueOf( level.toUpperCase() );
		def interval = newLevel.getInterval() as Long;
		def unitWidth = newLevel.getUnitWidth() as Long;
		xScale = (1000.0 * interval) / unitWidth;
		
		//timeSpan = 1000 * span * interval;
		zoomLevel = newLevel.getLevel();
		timeCalculator.setLevel( newLevel );
		showAll = newLevel == TotalTimeTickCalculator.Level.ALL;
		
		update();
	}
}

class ChartViewListener extends EventHandler {
	override function handleEvent( e:EventObject ):Void {
		if( e instanceof CollectionEvent ) {
			def event = e as CollectionEvent;
			if( CollectionEvent.Event.ADDED == event.getEvent() ) {
				FxUtils.runInFxThread( function():Void { addedSegment( event.getElement() as LineSegment ) } );
			} else {
				FxUtils.runInFxThread( function():Void { removedSegment( event.getElement() as LineSegment ) } );
			}
		} else if( e instanceof PropertyChangeEvent ) {
			def event = e as PropertyChangeEvent;
			if( ZoomPanel.ZOOM_LEVEL.equals( event.getPropertyName() ) ) {
				FxUtils.runInFxThread( function():Void { setZoomLevel( event.getNewValue() as String ) } );
			}
		}
	}
}