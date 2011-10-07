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

import javafx.util.Math;

import com.eviware.loadui.fx.statistics.StatisticsWindow;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.api.charting.line.LineChart;
import com.eviware.loadui.util.StringUtils;


def defaultChartGroupLabel = "Chart";

public function createStatisticsTab( pages:StatisticPages, label:String, sh:StatisticHolder ):StatisticPage {
	def pageLabel = if( StringUtils.isNullOrEmpty( label ) ) "Page {pages.getChildCount()+1}" else label;
	def page = pages.createPage( pageLabel );
	if( sh != null ) {
		createSubChart( createChartGroup( page, null, null ), sh );
	}
	
	return page;
}

public function createChartGroup( parent:StatisticPage, type:String, label:String ):ChartGroup {
	def groupLabel = if( StringUtils.isNullOrEmpty( label ) ) "Chart {parent.getChildCount()+1}" else label;
	def chartType = if( StringUtils.isNullOrEmpty( type ) ) LineChartView.class.getName() else type;
	def chartGroup = parent.createChartGroup( chartType, groupLabel );
	def chartView = chartGroup.getChartView();
	def mainLength = if( StatisticsWindow.currentExecution != null ) StatisticsWindow.currentExecution.getLength() else 0;
	def comparedLength = if( StatisticsWindow.comparedExecution != null ) StatisticsWindow.comparedExecution.getLength() else 0;
	def length = Math.max( mainLength, comparedLength );
	def zoomLevel = if( length < 4 * 60 * 1000 ) ZoomLevel.SECONDS
		else if( length < 4 * 3600 * 1000 ) ZoomLevel.MINUTES
		else if( length < 96 * 3600 * 1000 ) ZoomLevel.HOURS
		else if( length < 28 * 24 * 3600 * 1000 ) ZoomLevel.DAYS
		else ZoomLevel.WEEKS;
	chartView.setAttribute( LineChart.ZOOM_LEVEL_ATTRIBUTE, zoomLevel.name() );
	
	chartGroup
}

public function createSubChart( parent:ChartGroup, sh:StatisticHolder ):Chart {
	def chart = parent.createChart( sh );
	if( sh instanceof ComponentItem ) {
		def component:ComponentItem = sh as ComponentItem;
		
		if( component.getType().equals( "Assertion" ) ) {	
			def variable = component.getStatisticVariable( "Assertion Failures" );
			if( variable != null and variable.getStatisticNames().contains( "TOTAL" ) ) {
				def chartView = parent.getChartViewForChart( chart );
				(chartView as ConfigurableLineChartView).addSegment( "Assertion Failures", "TOTAL", StatisticVariable.MAIN_SOURCE );
			}
		} else {
			def variable = component.getStatisticVariable( "Time Taken" );
			if( variable != null and variable.getStatisticNames().contains( "AVERAGE" ) ) {
				def chartView = parent.getChartViewForChart( chart );
				(chartView as ConfigurableLineChartView).addSegment( "Time Taken", "AVERAGE", StatisticVariable.MAIN_SOURCE );
			}
		}
	} else if( sh instanceof CanvasItem ) {
		def variable = sh.getStatisticVariable( "Requests" );
		if( variable != null and variable.getStatisticNames().contains( "PER_SECOND" ) ) {
			def chartView = parent.getChartViewForChart( chart );
			(chartView as ConfigurableLineChartView).addSegment( "Requests", "PER_SECOND", StatisticVariable.MAIN_SOURCE );
		}
	}
	
	chart
}

public class ChartDefaults {
}
