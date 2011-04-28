package com.eviware.loadui.impl.charting.line;

import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.store.Execution;
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

	public AbstractLineSegmentModel( ChartStyle chartStyle )
	{
		this.chartStyle = chartStyle;
	}

	public ChartStyle getChartStyle()
	{
		return chartStyle;
	}

	public void setExecution( Execution execution )
	{
		if( this.execution != execution )
		{
			this.execution = execution;
			redraw();
		}
	}

	protected abstract void redraw();

	protected void doRedraw( Statistic<?> statistic, long xMin, long xMax, int level )
	{
		dataFetcher.queueRead( this, new PendingRead( statistic, xMin, xMax, level, scalar ) );
	}

	@Override
	public String getName()
	{
		return "";
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
			dataPoints = statistic.getPeriod( xMin, xMax, level, execution );
		}

		@Override
		public void run()
		{
			clearPoints();
			DataPoint<?> dataPoint = null;
			for( Object object : dataPoints )
			{
				dataPoint = ( DataPoint<?> )object;
				addPoint( dataPoint.getTimestamp(), scalar * dataPoint.getValue().doubleValue(), false );
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
		private final LinkedHashMap<Object, Runnable> queue = new LinkedHashMap<Object, Runnable>();

		@Override
		public void run()
		{
			Runnable task = null;

			while( true )
			{
				try
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
						catch( Exception e )
						{
							e.printStackTrace();
						}
					}
					if( task != null )
					{
						if( task instanceof PendingRead )
							( ( PendingRead )task ).read();
						SwingUtilities.invokeLater( task );
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}

		private void queueRead( Object key, Runnable read )
		{
			synchronized( queue )
			{
				queue.put( key, read );
				queue.notifyAll();
			}
		}
	}
}
