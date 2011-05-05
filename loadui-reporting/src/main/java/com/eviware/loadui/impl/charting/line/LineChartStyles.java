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
package com.eviware.loadui.impl.charting.line;

import java.awt.Color;
import java.util.LinkedHashSet;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;
import com.google.common.collect.Iterables;

public class LineChartStyles
{

	public static final String[] lineColors = new String[] { "#FF2100", "#FF7B00", "#00B700", "#00B2D2", "#7826B5",
			"#D7268E", "#FFA400", "#9BCD00", "#002AB6", "#007AC3", "#FFFB00", "#FFFFFF" };

	public static final Color CHART_BACKGROUND_COLOR = new Color( 0x1a, 0x1a, 0x1a, 0 );
	public static final Color CHART_FOREGROUND_COLOR = new Color( 0xcd, 0xcd, 0xcd );

	public static void styleChart( LineChartImpl chart )
	{
		chart.setAnimateOnShow( false );
		chart.setPanelBackground( CHART_BACKGROUND_COLOR );
		chart.setChartBackground( CHART_BACKGROUND_COLOR );
		chart.setLabelColor( CHART_FOREGROUND_COLOR );
		chart.setGridColor( CHART_FOREGROUND_COLOR );
		chart.setTickColor( CHART_FOREGROUND_COLOR );

		chart.setVerticalGridLinesVisible( false );
		chart.setHorizontalGridLinesVisible( false );
	}

	public static void styleChartForPrint( LineChartImpl chart )
	{
		chart.setAnimateOnShow( false );
		chart.setHighQuality( true );
		chart.setPanelBackground( Color.WHITE );
		chart.setChartBackground( Color.WHITE );

		chart.setLabelColor( Color.BLACK );
		chart.setGridColor( Color.BLACK );
		chart.setTickColor( Color.BLACK );

		chart.setVerticalGridLinesVisible( false );
		chart.setHorizontalGridLinesVisible( false );
	}

	public static String getLineColor( ChartGroup chartGroup, LineSegment segment )
	{
		LinkedHashSet<String> colors = new LinkedHashSet<String>();
		for( String color : lineColors )
			colors.add( color );

		for( ChartView chartView : chartGroup.getChartViewsForCharts() )
			for( LineSegment s : ( ( LineChartView )chartView ).getSegments() )
				if( s != segment )
					colors.remove( s.getAttribute( LineSegmentChartModel.COLOR, "" ) );

		return Iterables.getFirst( colors, lineColors[0] );
	}
}
