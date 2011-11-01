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
package com.eviware.loadui.impl.charting.line;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.store.Execution;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;

public abstract class AbstractLineSegmentModel extends DefaultChartModel
{
	private static final DataFetcher dataFetcher = new DataFetcher();
	public static final Logger log = LoggerFactory.getLogger( AbstractLineSegmentModel.class );

	static
	{
		Thread dataFetcherThread = new Thread( dataFetcher, "StatisticsDataFetcher" );
		dataFetcherThread.setDaemon( true );
		dataFetcherThread.start();
	}

	static void awaitQueuedReads()
	{
		new QueueWaiter().awaitDone();
	}

	protected final ChartStyle chartStyle;

	protected Execution execution;
	protected double scalar = 1;
	private final String name;
	private double minY = Double.POSITIVE_INFINITY;
	private double maxY = Double.NEGATIVE_INFINITY;

	public AbstractLineSegmentModel( String name, ChartStyle chartStyle )
	{
		this.name = name;
		this.chartStyle = chartStyle;
	}

	public ChartStyle getChartStyle()
	{
		return chartStyle;
	}

	public void setExecution( Execution execution )
	{
		this.execution = execution;
		redraw();
	}

	protected abstract void redraw();

	protected void doRedraw( Statistic<?> statistic, long xMin, long xMax, int level )
	{
		dataFetcher.queueRead( this, new PendingRead( statistic, xMin, xMax, level, scalar ) );
	}

	@Override
	public String getName()
	{
		return name;
	}

	public double getMaxY()
	{
		return maxY;
	}

	public double getMinY()
	{
		return minY;
	}

	@Override
	public DefaultChartModel addPoint( double x, double y, boolean flush )
	{
		minY = Math.min( minY, y );
		maxY = Math.max( maxY, y );

		return super.addPoint( x, y, flush );
	}

	@Override
	public void clearPoints()
	{
		super.clearPoints();

		minY = Double.POSITIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;
	}

	@Override
	public boolean equals( Object obj )
	{
		return this == obj;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode( name );
	}

	private class PendingRead implements Runnable
	{
		private final Statistic<?> statistic;
		private final long xMin, xMax;
		private final int level;
		private final double scalar;
		private Iterable<?> dataPoints;

		public PendingRead( Statistic<?> statistic, long xMin, long xMax, int level, double scalar )
		{
			this.statistic = statistic;
			this.xMin = xMin;
			this.xMax = xMax;
			this.level = level;
			this.scalar = scalar;
		}

		public void read()
		{
			dataPoints = execution == null ? Collections.emptyList() : statistic.getPeriod( xMin, xMax, level, execution );
		}

		@Override
		public void run()
		{
			clearPoints();
			DataPoint<?> dataPoint = null;
			for( Object object : dataPoints )
			{
				dataPoint = ( DataPoint<?> )object;
				final double doubleValue = dataPoint.getValue().doubleValue();
				if( !Double.isNaN( doubleValue ) )
					addPoint( dataPoint.getTimestamp(), scalar * doubleValue, false );
			}
			update();
		}
	}

	private static class QueueWaiter implements Runnable
	{
		private boolean done = false;

		public QueueWaiter()
		{
			dataFetcher.queueRead( this, this );
		}

		@Override
		public void run()
		{
			synchronized( this )
			{
				done = true;
				notifyAll();
			}
		}

		public void awaitDone()
		{
			if( SwingUtilities.isEventDispatchThread() )
			{
				log.error( "Cannot await Chart drawing in the Event Dispatch Thread! Charts may not be drawn!" );
				return;
			}

			synchronized( this )
			{
				if( !done )
				{
					try
					{
						wait( 5000 );
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
					}
				}
			}
			if( !done )
			{
				log.error( "Failed waiting for LineChart to complete drawing." );
			}
		}
	}

	private static class DataFetcher implements Runnable
	{
		private final LinkedHashMap<Object, Runnable> queue = Maps.newLinkedHashMap();

		@Override
		public void run()
		{
			Runnable task = null;

			while( true )
			{
				synchronized( queue )
				{
					try
					{
						if( queue.isEmpty() )
							queue.wait();

						Iterator<Runnable> iterator = queue.values().iterator();
						task = iterator.next();
						iterator.remove();
					}
					catch( InterruptedException e )
					{
						// Do nothing.
					}
				}
				if( task != null )
				{
					if( task instanceof PendingRead )
						( ( PendingRead )task ).read();
					SwingUtilities.invokeLater( task );
				}
			}
		}

		private void queueRead( Object reader, Runnable read )
		{
			synchronized( queue )
			{
				queue.put( reader, read );
				queue.notifyAll();
			}
		}
	}
}
