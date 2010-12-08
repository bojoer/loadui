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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Math;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;

import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.model.Releasable;
import java.awt.Color;
import java.util.EventObject;
import java.util.HashMap;

import javafx.ext.swing.SwingComponent;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.model.ChartPoint;
import com.jidesoft.range.TimeRange;
import com.jidesoft.chart.axis.TimeAxis;
import com.jidesoft.chart.style.ChartStyle;

/**
 * Base LineChart Node, visualizes a LineChartView.
 *
 * @author dain.nilsson
 */
public class LineChart extends BaseNode, Resizable, Releasable {
	def listener = new ChartViewListener();
	def lines = new HashMap();
	
	var min:Number = 0;
	var max:Number = 0;
	
	def timeline = Timeline {
		repeatCount: Timeline.INDEFINITE
		keyFrames: [
			KeyFrame {
				time: 1s
				action: function():Void {
					def time = java.lang.System.currentTimeMillis();
					for( key in lines.keySet() ) {
						def segment = key as LineSegment;
						def model = lines.get( key ) as DefaultChartModel;
						println("Adding point: {time}:{segment.getStatistic().getValue() as Number}");
						def yValue = segment.getStatistic().getValue() as Number;
						min = Math.min( min, yValue );
						max = Math.max( max, yValue );
						model.addPoint( new ChartPoint( time, yValue ) );
					}
					chart.getXAxis().setRange( new TimeRange( time - 10000, time ) );
					chart.getYAxis().setRange( min, max );
				}
			}
		]
	}
	
	public-init var chartView:LineChartView on replace oldChartView {
		if( chartView != null ) {
			chartView.addEventListener( CollectionEvent.class, listener );
			
			for( segment in chartView.getSegments() )
				addedSegment( segment );
				
			timeline.playFromStart();
			
			//TODO: Remove this when LineSegments are configurable within the gui.
			if( chartView instanceof ConfigurableLineChartView and chartView.getSegments().isEmpty() ) {
				def clcv = chartView as ConfigurableLineChartView;
				clcv.addSegment( "TimeTaken", "AVERAGE", "main" );
			}
		}
		
		if( oldChartView != null ) {
			chartView.removeEventListener( CollectionEvent.class, listener );
			timeline.stop();
			lines.clear();
		}
	}
	
	override var layoutInfo = LayoutInfo { vfill: true, hfill: true, vgrow: Priority.ALWAYS, hgrow: Priority.ALWAYS }
	
	def chart = new Chart();
	def chartNode = SwingComponent.wrap( chart );
	
	def resizable:VBox = VBox {
		width: bind width
		height: bind height
		content: Stack { content: chartNode }
	}
	
	init {
		chart.setChartBackground(new Color(0, 0, 0, 0));
		chart.setXAxis( new TimeAxis() );
		chart.setVerticalGridLinesVisible( false );
		chart.setHorizontalGridLinesVisible( false );
		
		chartNode.layoutInfo = LayoutInfo { height: 150, hfill: true, hgrow: Priority.ALWAYS }
		timeline.playFromStart();
	}
	
	override function release():Void {
		chartView = null;
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
		println("ADDED: {segment} to {chartView}");
		def model = new DefaultChartModel( "{chartView}" );
		lines.put( segment, model );
		def style = new ChartStyle( Color.blue, false, true );
		chart.addModel( model, style );
	}
	
	function removedSegment( segment:LineSegment ):Void {
		println("REMOVED: {segment} from {chartView}");
		def model = lines.remove( segment ) as DefaultChartModel;
		chart.removeModel( model );
	}
}

class ChartViewListener extends EventHandler {
	override function handleEvent( e:EventObject ):Void {
		def event = e as CollectionEvent;
		if( CollectionEvent.Event.ADDED == event.getEvent() ) {
			FxUtils.runInFxThread( function():Void { addedSegment( event.getElement() as LineSegment ) } );
		} else {
			FxUtils.runInFxThread( function():Void { removedSegment( event.getElement() as LineSegment ) } );
		}
	}
}