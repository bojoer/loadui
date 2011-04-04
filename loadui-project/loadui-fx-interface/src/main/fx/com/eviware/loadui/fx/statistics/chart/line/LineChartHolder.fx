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
import javafx.scene.layout.Resizable;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
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
import javafx.util.Math;

import javafx.ext.swing.SwingComponent;
import com.sun.javafx.scene.layout.Region;
import javax.swing.JComponent;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.fx.statistics.chart.BaseChart;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.api.charting.line.LineChart;
import com.eviware.loadui.api.charting.line.LineSegmentModel;
import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;

import java.util.WeakHashMap;

def lineChartCache = new WeakHashMap();

public function getLineChart( chartView:LineChartView ):LineChart {
	lineChartCache.get( chartView ) as LineChart
}

public class LineChartHolder extends BaseNode, Resizable, BaseChart, Releasable {
	override var styleClass = "line-chart";
	override var layoutInfo = LayoutInfo { vfill: true, hfill: true, vgrow: Priority.ALWAYS, hgrow: Priority.ALWAYS, minWidth: 200 }
	
	def lineChartFactory:LineChart.Factory = BeanInjector.getBean( LineChart.Factory.class );
	
	def segmentListener = new SegmentListener();
	
	public-init var chartView:LineChartView on replace {
		chart = if( chartView == null ) null else lineChartFactory.createLineChart( chartView );
		chart.addEventListener( CollectionEvent.class, segmentListener );
		lineChartCache.put( chartView, chart );
	}
	
	var chartNode:Node;
	
	var initialized = false;
	var showAll = false;
	var compactSegments = true;
	
	def execution = bind StatisticsWindow.execution on replace {
		if( execution != null ) chart.setMainExecution( execution );
	}
	
	def comparedExecution = bind StatisticsWindow.comparedExecution on replace {
		chart.setComparedExecution( comparedExecution );
	}
	
	var chart:LineChart on replace oldVal {
		if( chart != null ) {
			if( execution != null ) chart.setMainExecution( execution );
			chart.setComparedExecution( comparedExecution );
			def zoomLevel = chart.getZoomLevel();
			if( zoomLevel == ZoomLevel.ALL ) {
				showAll = true;
			} else {
				def unitWidth = zoomLevel.getUnitWidth();
				def interval = zoomLevel.getInterval();
				xScale = (1000.0 * interval) / unitWidth;
				def maxTime = chart.getMaxTime();
				def timeSpan = chart.getTimeSpan();
				if( maxTime > timeSpan ) {
					scrollBar.value = ( chart.getPosition() * maxTime ) / ( maxTime - timeSpan );
					scrollBar.visibleAmount = timeSpan;
					scrollBar.unitIncrement = timeSpan / 10;
					scrollBar.blockIncrement = timeSpan / 10;
				}
			}
			chartNode = SwingComponent.wrap( chart as JComponent );
			chartNode.layoutInfo = LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, vfill: true, vgrow: Priority.ALWAYS, margin: Insets { left: -15, right: 10 } };
		}
		
		if( oldVal != null ) {
			ReleasableUtils.release( oldVal );
			lineChartCache.remove( oldVal );
		}
	}
	
	def scrollBar = ScrollBar {
		vertical: false
		layoutInfo: LayoutInfo { hgrow: Priority.ALWAYS, hfill: true, margin: Insets { top: - 1, right: - 1, bottom: - 1, left: - 1 } }
		clickToPosition: true
	}
	
	def scrollBarPosition = bind scrollBar.value on replace oldVal {
		if( initialized ) {
			def maxTime = chart.getMaxTime();
			var sbPos = Math.min( maxTime, scrollBarPosition );
			if( maxTime - sbPos < 1000 ) {
				chart.setFollow( true );
			} else {
				chart.setFollow( false );
				def position = scrollBarPosition * ( maxTime - chart.getTimeSpan() ) / maxTime;
				if( Math.abs( chart.getPosition() - position ) > 10 )
					chart.setPosition( position );
			}
		}
	}
	
	def chartNodeWidth = bind (chartNode as Resizable).width - 20 on replace {
		if( not showAll ) chart.setTimeSpan( ( chartNodeWidth * xScale ) as Long );
	}
	
	var xScale:Number = 0.002 on replace {
		if( not showAll ) chart.setTimeSpan( ( chartNodeWidth * xScale ) as Long )
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
	
	override function update():Void {
		def follow = chart.isFollow();
		
		def unitWidth = chart.getZoomLevel().getUnitWidth();
		def interval = chart.getZoomLevel().getInterval();
		xScale = (1000.0 * interval) / unitWidth;
		
		chart.refresh( StatisticsWindow.execution == StatisticsWindow.currentExecution );
		def maxTime = chart.getMaxTime();
		def timeSpan = chart.getTimeSpan();
		
		if( maxTime > timeSpan ) {
			scrollBar.max = maxTime;
			scrollBar.value = if( follow ) maxTime else chart.getPosition() * maxTime / ( maxTime - timeSpan );
			scrollBar.visibleAmount = timeSpan;
			scrollBar.unitIncrement = timeSpan / 10;
			scrollBar.blockIncrement = timeSpan / 10;
		} else {
			scrollBar.max = 0;
			scrollBar.value = 0;
		}
	}
	
	override function reset():Void {
	}
	
	override function create():Node {
		for( segment in chartView.getSegments() )
			addSegment( chart.getLineSegmentModel( segment ) );
		
		initialized = true;
		
		resizable
	}
	
	override function getPrefHeight( width:Number ):Number {
		resizable.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ):Number {
		resizable.getPrefWidth( height )
	}
	
	override function release() {
		chart = null;
	}
	
	function addSegment( lineModel:LineSegmentModel ):Void {
		def segment = lineModel.getLineSegment();
		insert if( segment instanceof LineSegment.Removable ) {
			DraggableFrame {
				draggable: DeletableSegmentButton {
					segment: segment as LineSegment.Removable
					compactSegments: bind compactSegments
					chartView: chartView
					model: lineModel
					confirmDialogScene: bind scene
				}
			};
		} else {
			SegmentButton {
				compactSegments: bind compactSegments
				chartView: chartView
				model: lineModel
			};
		} into segmentButtons.content;
	}
	
	function removeSegment( lineModel:LineSegmentModel ):Void {
		def segment = lineModel.getLineSegment();
		for( frame in segmentButtons.content[b | b instanceof DraggableFrame] ) {
			def button = (frame as DraggableFrame).draggable as DeletableSegmentButton;
			if( button.model.getLineSegment() == segment )
				delete frame from segmentButtons.content;
		}
		for( button in segmentButtons.content[b | b instanceof SegmentButton] ) {
			if( (button as SegmentButton).model.getLineSegment() == segment )
				delete button from segmentButtons.content;
		}
	}
}

class SegmentListener extends WeakEventHandler {
	override function handleEvent( e ):Void {
		def event = e as CollectionEvent;
		if( LineChart.LINE_SEGMENT_MODELS.equals( event.getKey() ) ) {
			def segment = event.getElement() as LineSegmentModel;
			if( CollectionEvent.Event.ADDED == event.getEvent() ) {
				FxUtils.runInFxThread( function():Void { addSegment( segment ); } );
			} else {
				FxUtils.runInFxThread( function():Void { removeSegment( segment ); } );
			}
		}
	}
}