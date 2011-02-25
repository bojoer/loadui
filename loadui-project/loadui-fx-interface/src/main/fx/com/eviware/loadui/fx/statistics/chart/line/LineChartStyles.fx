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

import java.util.HashMap;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.style.ChartStyle;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;

public def chartBackgroundColor = new Color( 0x1a, 0x1a, 0x1a, 0 );
public def chartForegroundColor = new Color( 0xcd, 0xcd, 0xcd );
public def lineColor = Color.red;

public def chartBackgroundPrintColor = new Color( 0xff, 0xff, 0xff );
public def chartForegroundPrintColor = new Color( 0x0, 0x0, 0x0 );

public def solidStroke = [ 1.0, 0.0 ];
public def dashedStroke = [ 5.0, 6.0 ];
public def dottedStroke = [ 1.0, 2.0 ];

def lineColors = [
	"#FF2100",
	"#FF7B00",
	"#00B700",
	"#00B2D2",
	"#7826B5",
	"#D7268E",
	"#FFA400",
	"#9BCD00",
	"#002AB6",
	"#007AC3",
	"#FFFB00",
	"#FFFFFF"
];

public function styleChart( chart:Chart ):Void {
	chart.setPanelBackground( chartBackgroundColor );
	chart.setChartBackground( chartBackgroundColor );
	chart.setLabelColor( chartForegroundColor );
	chart.setGridColor( chartForegroundColor );
	chart.setTickColor( chartForegroundColor );
	
	chart.setVerticalGridLinesVisible( false );
	chart.setHorizontalGridLinesVisible( false );
}

public function styleChartForPrint( chart:Chart ):Void {
	chart.setHighQuality( true );
	chart.setPanelBackground( chartBackgroundPrintColor );
	chart.setChartBackground( chartBackgroundPrintColor );
	
	chart.setLabelColor( chartForegroundPrintColor );
	chart.setGridColor( chartForegroundPrintColor );
	chart.setTickColor( chartForegroundPrintColor );
	
	chart.setVerticalGridLinesVisible( false );
	chart.setHorizontalGridLinesVisible( false );
}

public function styleChartStyle( chartStyle:ChartStyle ):Void {
	chartStyle.setLineColor( lineColor );
	chartStyle.setLineStroke( getStroke( 1, solidStroke ) );
	
	chartStyle.setPointsVisible( false );
}

public function getStroke( width:Integer, style:Number[] ):BasicStroke {
	def scaledStyle = [ style[0]*width, style[1]*width ];
	new BasicStroke( width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, scaledStyle, 0 );
}

var statColorMap: HashMap;

public function getLineColor( chartGroup:ChartGroup, segment:LineSegment ):String {
	var colors = lineColors;
	for( chartView in chartGroup.getChartViewsForCharts() ) {
		for( s in (chartView as LineChartView).getSegments()[x|x != segment] ) {
			delete s.getAttribute( LineSegmentChartModel.COLOR, null ) from colors;
		}
	}
	return if( sizeof colors > 0 ) colors[0] else lineColors[0];
}

/**
 * Provides default colors for Line charts.
 *
 * @author predrag.vucetic
 * @author dain.nilsson
 */
public class LineChartStyles {
}