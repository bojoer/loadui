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
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.impl.charting.line.LineChartImpl;
import com.eviware.loadui.impl.charting.line.LineChartStyles;
import com.eviware.loadui.impl.charting.line.LongRange;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.jidesoft.chart.model.ChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.chart.util.ChartUtils;

public class LineChartUtils
{
	public static final int printScaleFactor = 4;

	public static Map<ChartView, Image> createImages( Collection<StatisticPage> pages, Execution mainExecution,
			Execution comparedExecution )
	{
		HashMap<ChartView, Image> images = new HashMap<ChartView, Image>();
		for( StatisticPage page : pages )
			for( ChartGroup chartGroup : page.getChildren() )
				images.putAll( createImages( chartGroup, mainExecution, comparedExecution ) );

		return images;
	}

	public static Map<ChartView, Image> createImages( ChartGroup chartGroup, Execution mainExecution,
			Execution comparedExecution )
	{
		HashMap<ChartView, Image> images = new HashMap<ChartView, Image>();

		ChartView groupChartView = chartGroup.getChartView();
		images.put( groupChartView, generateChartImage( groupChartView, mainExecution, comparedExecution ) );

		String expand = chartGroup.getAttribute( "expand", "none" );
		if( "group".equals( expand ) )
		{
			for( Chart chart : chartGroup.getChildren() )
			{
				ChartView chartView = chartGroup.getChartViewForChart( chart );
				images.put( chartView, generateChartImage( chartView, mainExecution, comparedExecution ) );
			}
		}
		else if( "sources".equals( expand ) )
		{
			for( String source : chartGroup.getSources() )
			{
				ChartView chartView = chartGroup.getChartViewForSource( source );
				images.put( chartView, generateChartImage( chartView, mainExecution, comparedExecution ) );
			}
		}

		return images;
	}

	public static Image generateChartImage( ChartView chartView, Execution mainExecution, Execution comparedExecution )
	{
		if( chartView instanceof LineChartView )
		{
			int height = Math.max( ( int )Double.parseDouble( chartView.getAttribute( "height", "0" ) ), 100 );
			return LineChartUtils.createImage( ( LineChartView )chartView, 505, height, mainExecution, comparedExecution );
		}
		return null;
	}

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

		chart.awaitDraw();
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

		chart.setTimeSpanNoSave( 10000 );

		LongRange range = ( LongRange )chart.getXAxis().getRange();
		range.setMax( range.lower() + 10000 );
		chart.setAxisLabelPadding( -6 );

		chart.awaitDraw();
		chart.update();

		Image image = ChartUtils.createImage( chart );
		ReleasableUtils.release( chart );

		return image;
	}

	public static void updateExecutionIcon( final Execution execution, final LineChartView chartView,
			final Execution comparedExecution )
	{
		BeanInjector.getBean( ExecutorService.class ).execute( new Runnable()
		{
			@Override
			public void run()
			{
				execution.setIcon( LineChartUtils.createThumbnail( chartView, execution, comparedExecution ) );
				execution.fireEvent( new BaseEvent( execution, Execution.ICON ) );
			}
		} );
	}

	public static void invokeInSwingAndWait( Runnable runnable ) throws InterruptedException, InvocationTargetException
	{
		if( SwingUtilities.isEventDispatchThread() )
			runnable.run();
		else
			SwingUtilities.invokeAndWait( runnable );
	}

	public static void invokeInSwingLater( Runnable runnable )
	{
		if( SwingUtilities.isEventDispatchThread() )
			runnable.run();
		else
			SwingUtilities.invokeLater( runnable );
	}
}
