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
package com.eviware.loadui.ui.fx.views.analysis.reporting;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.google.common.collect.Maps;

public class LineChartUtils
{
	public static final int printScaleFactor = 4;

	public static Map<ChartView, Image> createImages( Collection<StatisticPage> pages,
			ObservableValue<Execution> executionProperty, Execution comparedExecution )
	{
		HashMap<ChartView, Image> images = Maps.newHashMap();
		for( StatisticPage page : pages )
			for( ChartGroup chartGroup : page.getChildren() )
				images.putAll( createImages( chartGroup, executionProperty, comparedExecution ) );

		return images;
	}

	public static Map<ChartView, Image> createImages( ChartGroup chartGroup,
			ObservableValue<Execution> executionProperty, Execution comparedExecution )
	{
		HashMap<ChartView, Image> images = new HashMap<>();

		ChartView groupChartView = chartGroup.getChartView();
		images.put( groupChartView, generateChartImage( groupChartView, executionProperty, comparedExecution ) );

		String expand = chartGroup.getAttribute( "expand", "none" );
		if( "group".equals( expand ) )
		{
			for( Chart chart : chartGroup.getChildren() )
			{
				ChartView chartView = chartGroup.getChartViewForChart( chart );
				images.put( chartView, generateChartImage( chartView, executionProperty, comparedExecution ) );
			}
		}
		else if( "sources".equals( expand ) )
		{
			for( String source : chartGroup.getSources() )
			{
				ChartView chartView = chartGroup.getChartViewForSource( source );
				images.put( chartView, generateChartImage( chartView, executionProperty, comparedExecution ) );
			}
		}

		return images;
	}

	public static Image generateChartImage( ChartView chartView, ObservableValue<Execution> executionProperty,
			Execution comparedExecution )
	{
		if( chartView instanceof LineChartView )
		{
			int height = Math.max( ( int )Double.parseDouble( chartView.getAttribute( "height", "0" ) ), 100 );
			return createImage( ( LineChartView )chartView, 505, height, executionProperty, comparedExecution );
		}
		return null;
	}

	public static Image createImage( LineChartView chartView, int width, int height,
			ObservableValue<Execution> executionProperty, Execution comparedExecution )
	{
		//TODO: replace with JavaFX 2 classes
		//		LineChartViewNode chartViewNode = new LineChartViewNode( executionProperty, chartView, new ObservableBase() );
		//
		//		//		LineChartImpl chart = new LineChartImpl( chartView );
		//		//		chart.setMainExecution( mainExecution );
		//		//		chart.setComparedExecution( comparedExecution );
		//		//		chart.setSize( new Dimension( width * printScaleFactor, height * printScaleFactor ) );
		//
		//		//		LineChartStyles.styleChartForPrint( chart );
		//
		//		//		Font font = chart.getTickFont();
		//		//		chart.setTickFont( new Font( font.getName(), font.getStyle(), font.getSize() * printScaleFactor / 2 ) );
		//		//		chart.setTickStroke( new BasicStroke( printScaleFactor ) );
		//
		//		//		for( ChartModel model : chart.getModels() )
		//		//		{
		//		//			ChartStyle style = chart.getStyle( model );
		//		//			BasicStroke stroke = style.getLineStroke();
		//		//			style.setLineStroke( new BasicStroke( printScaleFactor * stroke.getLineWidth(), stroke.getEndCap(), stroke
		//		//					.getLineJoin() ) );
		//		//		}
		//
		//		new Scene( chartViewNode );
		//
		//		Node node = chartViewNode.getLineChart();

		//		final WritableImage writableImage = node.snapshot( null, null );

		BufferedImage image = SwingFXUtils.fromFXImage( ( new Button() ).snapshot( null, null ), null );

		try
		{
			// ************************* TODO: Bug! This image is empty!!! Probably because the node is not added to a scene! *******************************************
			ImageIO.write( image, "png", new File( "out.png" ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return image;

		//		chart.awaitDraw();
		//		chart.update();

		//		Image image = ChartUtils.createImage( chart );
		//		ReleasableUtils.release( chart );
	}

	//	public static Image createThumbnail( LineChartView chartView, Execution mainExecution, Execution comparedExecution )
	//	{
	//		return new ImageCreator( chartView, mainExecution, comparedExecution ).getImage();
	//	}

	//	public static void updateExecutionIcon( final Execution execution, final LineChartView chartView,
	//			final Execution comparedExecution )
	//	{
	//		BeanInjector.getBean( ExecutorService.class ).execute( new Runnable()
	//		{
	//			@Override
	//			public void run()
	//			{
	//				execution.setIcon( LineChartUtils.createThumbnail( chartView, execution, comparedExecution ) );
	//				execution.fireEvent( new BaseEvent( execution, Execution.ICON ) );
	//			}
	//		} );
	//	}

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

	//	private static class ImageCreator
	//	{
	//		private final LineChartView chartView;
	//		private final Execution mainExecution;
	//		private final Execution comparedExecution;
	//		private LineChartImpl chart;
	//
	//		public ImageCreator( LineChartView chartView, Execution mainExecution, Execution comparedExecution )
	//		{
	//			this.chartView = chartView;
	//			this.mainExecution = mainExecution;
	//			this.comparedExecution = comparedExecution;
	//		}
	//
	//		public Image getImage()
	//		{
	//			try
	//			{
	//				invokeInSwingAndWait( new Runnable()
	//				{
	//					@Override
	//					public void run()
	//					{
	//						chart = new LineChartImpl( chartView );
	//						chart.setMainExecution( mainExecution );
	//						chart.setComparedExecution( comparedExecution );
	//						chart.setSize( new Dimension( 128, 56 ) );
	//
	//						chart.setAnimateOnShow( false );
	//						LineChartStyles.styleChart( chart );
	//						chart.setPanelBackground( Color.BLACK );
	//
	//						Font font = chart.getTickFont();
	//						chart.setTickFont( new Font( font.getName(), font.getStyle(), 4 ) );
	//
	//						chart.setTimeSpanNoSave( 10000 );
	//
	//						LongRange range = ( LongRange )chart.getXAxis().getRange();
	//						range.setMax( range.lower() + 10000 );
	//						chart.setAxisLabelPadding( -6 );
	//					}
	//				} );
	//			}
	//			catch( InterruptedException e )
	//			{
	//				e.printStackTrace();
	//			}
	//			catch( InvocationTargetException e )
	//			{
	//				e.printStackTrace();
	//			}
	//
	//			chart.awaitDraw();
	//			chart.update();
	//
	//			Image image = ChartUtils.createImage( chart );
	//			ReleasableUtils.release( chart );
	//
	//			return image;
	//		}
	//	}
}
