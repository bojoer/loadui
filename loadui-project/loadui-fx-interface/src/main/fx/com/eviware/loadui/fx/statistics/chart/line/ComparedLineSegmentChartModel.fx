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

import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.api.statistics.DataPoint;

import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.model.ChartModelListener;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.chart.util.ColorFactory;

public class ComparedLineSegmentChartModel extends DefaultChartModel {
	def listener = new Listener();
	
	public-read var chartStyle:ChartStyle;
	
	public-init var baseModel:LineSegmentChartModel on replace {
		baseModel.addChartModelListener( listener );
		chartStyle = new ChartStyle( baseModel.chartStyle );
	}
	
	def stroke = bind baseModel.stroke on replace {
		updateStroke();
	}
	
	def width = bind baseModel.width on replace {
		updateStroke();
	}
	
	def color = bind baseModel.color on replace {
		chartStyle.setLineColor( ColorFactory.intensify( FxUtils.getAwtColor( color ), -100 ) );
	}
	
	function updateStroke():Void {
		def newStroke = if( baseModel.stroke == "dashed" ) {
			LineChartStyles.dashedStroke
		} else if( stroke == "dotted" ) {
			LineChartStyles.dottedStroke
		} else {
			LineChartStyles.solidStroke
		}
		
		chartStyle.setLineStroke( LineChartStyles.getStroke( baseModel.width, newStroke ) );
		fireModelChanged();
	}
}

class Listener extends ChartModelListener {
	override function chartModelChanged() {
		clearPoints();
		for( dataPoint in baseModel.statistic.getPeriod( baseModel.xRange[0], baseModel.xRange[1], baseModel.level, StatisticsWindow.comparedExecution ) ) {
			addPoint( baseModel.scaler.createPoint( (dataPoint as DataPoint).getTimestamp(), (dataPoint as DataPoint).getValue() as Number ), false );
		}
		update();
	}
}