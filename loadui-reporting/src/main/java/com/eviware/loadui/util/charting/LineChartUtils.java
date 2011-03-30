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
package com.eviware.loadui.util.charting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;

import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.impl.charting.line.LineChartImpl;
import com.eviware.loadui.impl.charting.line.LineChartStyles;
import com.eviware.loadui.impl.charting.line.LongRange;
import com.eviware.loadui.util.ReleasableUtils;
import com.jidesoft.chart.model.ChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.chart.util.ChartUtils;

public class LineChartUtils
{
	public static final int printScaleFactor = 4;

	public static Image createImage( LineChartView chartView, int width, int height, Execution mainExecution,
			Execution comparedExecution )
	{
		LineChartImpl chart = new LineChartImpl( chartView );
		chart.setMainExecution( mainExecution );
		chart.setComparedExecution( comparedExecution );
		chart.setSize( new Dimension( width * printScaleFactor, height * printScaleFactor ) );

		LineChartStyles.styleChartForPrint( chart );

		Font font = chart.getTickFont();
		chart.setTickFont( new Font( font.getName(), font.getStyle(), font.getSize() * printScaleFactor / 2 ) );
		chart.setTickStroke( new BasicStroke( printScaleFactor ) );

		for( ChartModel model : chart.getModels() )
		{
			ChartStyle style = chart.getStyle( model );
			BasicStroke stroke = style.getLineStroke();
			style.setLineStroke( new BasicStroke( printScaleFactor * stroke.getLineWidth(), stroke.getEndCap(), stroke
					.getLineJoin() ) );
		}

		chart.refresh( false );
		chart.update();

		Image image = ChartUtils.createImage( chart );
		ReleasableUtils.release( chart );

		return image;
	}

	public static Image createThumbnail( LineChartView chartView, Execution mainExecution, Execution comparedExecution )
	{
		LineChartImpl chart = new LineChartImpl( chartView );
		chart.setMainExecution( mainExecution );
		chart.setComparedExecution( comparedExecution );
		chart.setSize( new Dimension( 128, 56 ) );

		chart.setAnimateOnShow( false );
		LineChartStyles.styleChart( chart );
		chart.setPanelBackground( Color.BLACK );

		Font font = chart.getTickFont();
		chart.setTickFont( new Font( font.getName(), font.getStyle(), 4 ) );

		chart.setTimeSpan( 10000 );

		LongRange range = ( LongRange )chart.getXAxis().getRange();
		range.setMax( range.lower() + 10000 );
		chart.setAxisLabelPadding( -6 );

		chart.refresh( false );
		chart.update();

		Image image = ChartUtils.createImage( chart );
		ReleasableUtils.release( chart );

		return image;
	}
}
