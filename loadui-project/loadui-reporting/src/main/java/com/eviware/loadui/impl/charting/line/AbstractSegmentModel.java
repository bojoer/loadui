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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;

public abstract class AbstractSegmentModel extends DefaultChartModel
{
	protected static final Logger log = LoggerFactory.getLogger( AbstractSegmentModel.class );
	private static final DataFetcher dataFetcher = new DataFetcher();

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
	protected final Segment segment;
	private final String name;
	protected Execution execution;

	public AbstractSegmentModel( Segment segment, String name, ChartStyle chartStyle )
	{
		this.segment = segment;
		this.chartStyle = chartStyle;
		this.name = name;
	}

	public Segment getSegment()
	{
		return segment;
	}

	@Override
	public String getName()
	{
		return name;
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

	protected void appendRead( Callable<? extends Iterable<DataPoint<?>>> readAction, double scalar )
	{
		dataFetcher.queueRead( this, new PendingRead( readAction, scalar ) );
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
		private final Callable<? extends Iterable<DataPoint<?>>> readAction;
		private final double scalar;
		private Iterable<DataPoint<?>> dataPoints;

		private PendingRead( Callable<? extends Iterable<DataPoint<?>>> readAction, double scalar )
		{
			this.readAction = readAction;
			this.scalar = scalar;
		}

		public void read()
		{
			try
			{
				dataPoints = readAction.call();
			}
			catch( Exception e )
			{
				log.error( "Unable to perform read", e );
			}
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
					{
						( ( PendingRead )task ).read();
					}
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
