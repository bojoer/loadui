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

import javafx.util.Math;
import javafx.scene.paint.Color;

import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import java.beans.PropertyChangeEvent;

import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;

public def SCALE = "scale";
public def COLOR = "color";
public def STROKE = "stroke";
public def WIDTH = "width";

public class LineSegmentChartModel extends DefaultChartModel, LineSegmentChartModelBase {
	public-read var latestTime:Number;
	
	public def scaler = new ScaledPointScale();
	
	def execution = bind StatisticsWindow.execution on replace {
		latestTime = execution.getLength();
	}
	
	def listener = new StyleEventListener();
	var chartGroup:ChartGroup;
	public-init var chartView:LineChartView on replace {
		chartGroup = chartView.getChartGroup();
		chartGroup.addEventListener( PropertyChangeEvent.class, listener );
	}
	
	public-init var segment:LineSegment on replace {
		statistic = segment.getStatistic();
		latestTime = statistic.getTimestamp();
	}
	
	public-read var statistic:Statistic;
	var initialized = false;
	
	public var level:Integer = 0 on replace {
	}
	
	public var xRange:Number[] = [ 0, 0 ] on replace oldXRange {
		refresh();
	}
	
	public var scale:Integer on replace oldScale {
		scaler.setScale( Math.pow( 10, scale ) );
		fireModelChanged();
		if( initialized ) {
			segment.setAttribute( SCALE, "{scale}" );
			chartGroup.fireEvent( new PropertyChangeEvent( segment, SCALE, oldScale, scale ) );
		}
		refresh();
	}
	
	public var color:Color on replace oldColor {
		chartStyle.setLineColor( FxUtils.getAwtColor( color ) );
		if( initialized ) {
			segment.setAttribute( COLOR, FxUtils.colorToWebString( color ) );
			chartGroup.fireEvent( new PropertyChangeEvent( segment, COLOR, oldColor, color ) );
		}
	}
	
	public var stroke:String on replace oldStroke {
		if( initialized ) {
			segment.setAttribute( STROKE, stroke );
			chartGroup.fireEvent( new PropertyChangeEvent( segment, STROKE, oldStroke, stroke ) );
			updateStroke();
		}
	}
	
	public var width:Integer on replace oldWidth {
		if( initialized ) {
			segment.setAttribute( WIDTH, "{width}" );
			chartGroup.fireEvent( new PropertyChangeEvent( segment, WIDTH, oldWidth, width ) );
			updateStroke();
		}
	}
	
	postinit {
		loadStyles();
		initialized = true;
		refresh();
	}
	
	public function poll():Void {
		def dataPoint = statistic.getLatestPoint( level );
		if( dataPoint != null ) {
			def timestamp = dataPoint.getTimestamp();
			if( timestamp != latestTime and timestamp >= 0 ) {
				latestTime = timestamp;
				if( xRange[0] <= timestamp and timestamp <= xRange[1] )
					addPoint( scaler.createPoint( timestamp, dataPoint.getValue() as Number ) );
			}
		}
	}
	
	function refresh():Void {
		clearPoints();
		for( dataPoint in statistic.getPeriod( xRange[0], xRange[1], level, StatisticsWindow.execution ) ) {
			addPoint( scaler.createPoint( (dataPoint as DataPoint).getTimestamp(), (dataPoint as DataPoint).getValue() as Number ), false );
		}
		update();
	}
	
	function loadStyles() {
		try {
			scale = Integer.parseInt( segment.getAttribute( SCALE, "0" ) );
		} catch(e) {
			scale = 0;
		}
		
		var colorStr = segment.getAttribute( COLOR, null );
		if( colorStr == null ) {
			colorStr = LineChartStyles.getLineColor( chartGroup, segment );
			segment.setAttribute( COLOR, colorStr );
		}
		chartStyle.setLineColor( FxUtils.getAwtColor( colorStr ) );
		color = Color.web( colorStr );
		
		try {
			width = Integer.parseInt( segment.getAttribute( WIDTH, "1" ) );
		} catch(e) {
			width = 1;
		}
		
		stroke = segment.getAttribute( STROKE, "solid" );
		
		updateStroke();
	}
	
	function updateStroke():Void {
		def newStroke = if( stroke == "dashed" ) {
			LineChartStyles.dashedStroke
		} else if( stroke == "dotted" ) {
			LineChartStyles.dottedStroke
		} else {
			LineChartStyles.solidStroke
		}
		
		chartStyle.setLineStroke( LineChartStyles.getStroke( width, newStroke ) );
		fireModelChanged();
	}
}

class StyleEventListener extends EventHandler {
	override function handleEvent( e ) {
		def event = e as PropertyChangeEvent;
		if( event.getSource() == segment ) {
			FxUtils.runInFxThread( function():Void {
				def isInitialized = initialized;
				initialized = false;
				if( SCALE.equals( event.getPropertyName() ) ) {
					scale = event.getNewValue() as Integer;
				} else if( COLOR.equals( event.getPropertyName() ) ) {
					color = event.getNewValue() as Color;
				} else if( STROKE.equals( event.getPropertyName() ) ) {
					stroke = event.getNewValue() as String;
					updateStroke();
				} else if( WIDTH.equals( event.getPropertyName() ) ) {
					width = event.getNewValue() as Integer;
					updateStroke();
				}
				initialized = isInitialized;
			} );
		}
	}
}