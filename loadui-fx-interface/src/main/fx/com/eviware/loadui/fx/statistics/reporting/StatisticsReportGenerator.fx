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
package com.eviware.loadui.fx.statistics.reporting;

import javafx.util.Math;

import com.eviware.loadui.fx.statistics.StatisticsWindow;
import com.eviware.loadui.fx.statistics.chart.BaseChart;
import com.eviware.loadui.fx.statistics.chart.ChartPage;
import com.eviware.loadui.fx.statistics.chart.ChartViewHolder;
import com.eviware.loadui.util.charting.LineChartUtils;

import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;

import java.util.Map;
import java.util.HashMap;
import java.awt.Image;

public function generateCharts( chartPages:ChartPage[] ):Map {
	def map = new HashMap();
	for( chartPage in chartPages ) {
		chartPage.layout();
		chartPage.update();
		for( chartGroupHolder in chartPage.innerContent ) {
			for( chartViewHolder in [ chartGroupHolder.chartViewHolder, chartGroupHolder.expandedChartViews ] ) {
				def image = createImage( chartViewHolder.chartView, Math.max( 150, chartViewHolder.chartHeight ) - 30 );
				if( image != null )
					map.put( chartViewHolder.chartView, image );
			}
		}
	}
	
	return map;
} 

function createImage( chartView:ChartView, height:Integer ):Image {
	if( chartView instanceof LineChartView ) {
		LineChartUtils.createImage( chartView as LineChartView, 505, height, StatisticsWindow.execution, StatisticsWindow.comparedExecution );
	} else {
		null
	}
}

public class StatisticsReportGenerator {
}