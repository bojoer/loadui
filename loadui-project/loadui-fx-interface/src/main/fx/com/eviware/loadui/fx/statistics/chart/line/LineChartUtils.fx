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

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.axis.TimeAxis;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.chart.util.ChartUtils;

import java.io.File;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.JLabel;

public def printScaleFactor = 4;

public function saveThumbnail( lineChart:LineChart, file:File ):Void {
	def chart = new Chart( new Dimension( 128, 56 ) );
	chart.setAnimateOnShow( false );
	LineChartStyles.styleChart( chart );
	chart.setPanelBackground( Color.BLACK );
	
	def font = chart.getTickFont();
	chart.setTickFont( new Font( font.getName(), font.getStyle(), 4 ) );
	
	chart.setXAxis( lineChart.chart.getXAxis() );
	chart.setYAxis( lineChart.chart.getYAxis() );
	def range = chart.getXAxis().getRange();
	chart.setXAxis( new TimeAxis( range.lower() as Long, (range.lower() as Long) + 10000 ));
	chart.setAxisLabelPadding( -6 ); 
	
	for( model in lineChart.chart.getModels()[x|x instanceof LineSegmentChartModel] ) {
		def lineSegment = model as LineSegmentChartModel;
		chart.addModel( lineSegment, lineSegment.chartStyle );
	}
	
	chart.update();
	
	
	/*def chart = lineChart.chart;
	def color = chart.getPanelBackground();
	chart.setPanelBackground( Color.BLACK );
	def imageIcon = ChartUtils.createThumbnail( chart, 128, 56 );
	chart.setPanelBackground( color );
	def label = new JLabel( imageIcon );
	label.setSize( imageIcon.getIconWidth(), imageIcon.getIconHeight() );*/
	ChartUtils.writePngToFile( chart, file );
}

public function createThumbnail( lineChart:LineChart ):Image {
	def chart = new Chart( new Dimension( 128, 56 ) );
	chart.setAnimateOnShow( false );
	LineChartStyles.styleChart( chart );
	chart.setPanelBackground( Color.BLACK );
	
	def font = chart.getTickFont();
	chart.setTickFont( new Font( font.getName(), font.getStyle(), 4 ) );
	
	chart.setXAxis( lineChart.chart.getXAxis() );
	chart.setYAxis( lineChart.chart.getYAxis() );
	def range = chart.getXAxis().getRange();
	chart.setXAxis( new TimeAxis( range.lower() as Long, (range.lower() as Long) + 10000 ));
	chart.setAxisLabelPadding( -6 ); 
	
	for( model in lineChart.chart.getModels()[x|x instanceof LineSegmentChartModel] ) {
		def lineSegment = model as LineSegmentChartModel;
		chart.addModel( lineSegment, lineSegment.chartStyle );
	}
	
	chart.update();
	ChartUtils.createImage( chart );
}

public function createImage( lineChart:LineChart, width:Integer, height:Integer ):Image {
	def chart = new Chart( new Dimension( width*printScaleFactor, height*printScaleFactor ));
	chart.setAnimateOnShow( false );
	LineChartStyles.styleChartForPrint( chart );
	
	def font = chart.getTickFont();
	chart.setTickFont( new Font( font.getName(), font.getStyle(), font.getSize()*printScaleFactor/2 ) );
	chart.setTickStroke( new BasicStroke( printScaleFactor ) );
	
	chart.setXAxis( lineChart.chart.getXAxis() );
	chart.setYAxis( lineChart.chart.getYAxis() );
	
	for( model in lineChart.chart.getModels()[x|x instanceof LineSegmentChartModelBase] ) {
		def lineSegment = (model as LineSegmentChartModelBase);
		def chartStyle = new ChartStyle( lineSegment.chartStyle );
		def stroke = chartStyle.getLineStroke();
		chartStyle.setLineStroke( new BasicStroke( printScaleFactor*stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin() ) );
		chart.addModel( lineSegment, chartStyle );
	}
	
	chart.update();
	
	return ChartUtils.createImage( chart );
}