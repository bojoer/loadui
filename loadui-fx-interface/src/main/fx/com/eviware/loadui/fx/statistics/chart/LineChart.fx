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
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.util.Math;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.treeselector.CascadingTreeSelector;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.model.Releasable;
import java.awt.BasicStroke;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Set;
import java.lang.Runnable;

import javafx.ext.swing.SwingComponent;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.model.ChartPoint;
import com.jidesoft.range.TimeRange;
import com.jidesoft.chart.axis.NumericAxis;
import com.jidesoft.chart.style.ChartStyle;

/**
 * Base LineChart Node, visualizes a LineChartView.
 *
 * @author dain.nilsson
 */
public class LineChart extends BaseNode, Resizable, BaseChart, Releasable {
	def listener = new ChartViewListener();
	def lines = new HashMap();
	def chart = new Chart();
	def chartNode = SwingComponent.wrap( chart );
	
	var padding = 2;
	var min:Number = 0;
	var max:Number = 0;
	var maxTime:Number = 0;
	
	def segmentButtons = VBox {
		layoutInfo: LayoutInfo { hgrow: Priority.NEVER, hfill: false },
		content: Rectangle { width: 1, height: 1, managed: false, fill: Color.rgb(0,0,0,0.0001) }
	}
	
	public-init var chartView:LineChartView on replace oldChartView {
		if( chartView != null ) {
			chartView.addEventListener( CollectionEvent.class, listener );
			
			for( segment in chartView.getSegments() )
				addedSegment( segment );
		}
		
		if( oldChartView != null ) {
			chartView.removeEventListener( CollectionEvent.class, listener );
			lines.clear();
		}
	}
	
	override var layoutInfo = LayoutInfo { vfill: true, hfill: true, vgrow: Priority.ALWAYS, hgrow: Priority.ALWAYS }
	
	def resizable:VBox = VBox {
		width: bind width
		height: bind height
		content: [
			HBox {
				content: [
					segmentButtons, Stack { content: chartNode }
				]
			}, if( chartView instanceof ConfigurableLineChartView ) Button {
				text: "Add Segment"
				action: function():Void {
					var selected:Runnable;
					holder.showConfig( VBox {
						content: [
							CascadingTreeSelector {
								treeModel: new SegmentTreeModel( chartView as ConfigurableLineChartView )
								allowMultiple: false
								onSelect: function(obj):Void { selected = obj as Runnable; }
								onDeselect: function(obj):Void { selected = null; }
							}, HBox {
								hpos: HPos.RIGHT
								content: [
									Button { text: "Add", disable: bind selected == null; action: function():Void { selected.run(); holder.hideConfig() } }
									Button { text: "Cancel", action: function():Void { holder.hideConfig() } }
								]
							}
						]
					} );
				}
			} else null
		]
	}
	
	init {
		chart.setChartBackground( new java.awt.Color(0, 0, 0, 0) );
		chart.setXAxis( new NumericAxis() );
		chart.setVerticalGridLinesVisible( false );
		chart.setHorizontalGridLinesVisible( false );
		
		chart.getXAxis().setRange( new TimeRange( 0, 10000 ) );
		chart.getYAxis().setRange( 0, 10 );
		
		chartNode.layoutInfo = LayoutInfo { height: 150, hfill: true, hgrow: Priority.ALWAYS };
	}
	
	override function update():Void {
		for( model in lines.values() ) {
			(model as LineSegmentModel).refresh();
		}
		chart.getXAxis().setRange( new TimeRange( maxTime - 10000, maxTime ) );
		chart.getYAxis().setRange( min - padding, max + padding );
	}
	
	override function reset():Void {
		for( model in lines.values() )
			(model as DefaultChartModel).clearPoints();
		maxTime = 0;
		max = 0;
		min = 0;
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
		def model = LineSegmentModel { segment: segment };
		lines.put( segment, model );
		chart.addModel( model, model.style );
		insert SegmentButton { model: model } into segmentButtons.content;
	}
	
	function removedSegment( segment:LineSegment ):Void {
		def model = lines.remove( segment ) as DefaultChartModel;
		chart.removeModel( model );
		for( button in segmentButtons.content[b|b instanceof SegmentButton] ) {
			if( (button as SegmentButton).model.segment == segment )
				delete button from segmentButtons.content;
		}
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

class SegmentButton extends Button {
	public-init var model:LineSegmentModel on replace {
		text = model.segment.getStatistic().getName();
	}
	
	override var action = function():Void {
		//TODO: Show configuration panel instead of removing the segment.
		if( chartView instanceof ConfigurableLineChartView )
			(chartView as ConfigurableLineChartView).removeSegment( model.segment );
	}
}

class LineSegmentModel extends DefaultChartModel {
	var timestamp = -1;
	var statistic:Statistic;
	
	public-read var style = new LineChartStyle();
	
	public-init var segment:LineSegment on replace {
		statistic = segment.getStatistic();
		
		def latestTime = statistic.getTimestamp();
		if( latestTime >= 0 ) {
			def startTime = Math.max( 0, latestTime - 11000 );
			for( dataPoint in statistic.getPeriod( startTime, latestTime ) ) {
				def yValue = (dataPoint as DataPoint).getValue() as Number;
				min = Math.min( min, yValue );
				max = Math.max( max, yValue );
				addPoint( new ChartPoint( (dataPoint as DataPoint).getTimestamp(), yValue ) );
			}
			maxTime = Math.max( maxTime, latestTime );
		}
	}
	
	public function refresh():Void {
		def latestTime = statistic.getTimestamp();
		if( timestamp != latestTime ) {
			timestamp = latestTime;
			def yValue = statistic.getValue() as Number;
			min = Math.min( min, yValue );
			max = Math.max( max, yValue );
			maxTime = Math.max( maxTime, timestamp );
			addPoint( new ChartPoint( timestamp, yValue ) );
		}
	}
}

class LineChartStyle extends ChartStyle {
    
    public var lineWidth: Integer = 1 on replace {
    	setLineWidth(lineWidth);
    }  

    public var strokeType: String = "Solid" on replace {
     	applyLineStroke();
    }

	 //how to set default color for each line. Is it per statistic?
	 public var lineColor: java.awt.Color = java.awt.Color.blue on replace {
	 	setLineColor(lineColor);
	 }
	 
    var dashMap: HashMap;
    
    init {
        setPointsVisible(false);
    }
    
    function applyLineStroke(){
  		  initDashMap();
        def stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, dashMap.get(strokeType) as Float[], 0);
        setLineStroke(stroke);
    }

    function initDashMap() {
        if(dashMap == null){
	        dashMap = new HashMap();
		     dashMap.put("Solid", [1.0]);
		     dashMap.put("Dashed", [8.0, 8.0]);
		     dashMap.put("Dotted", [2.0, 2.0]);
	     }
    }
    
    public function getStrokeTypes(): Set {
        dashMap.keySet();
    }
}






























