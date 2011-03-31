package com.eviware.loadui.impl.charting.line;

import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.SwingUtilities;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.store.Execution;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;

public abstract class AbstractLineSegmentModel extends DefaultChartModel
{
	private final static DataFetcher dataFetcher = new DataFetcher();

	static
	{
		Thread dataFetcherThread = new Thread( dataFetcher, "StatisticsDataFetcher" );
		dataFetcherThread.setDaemon( true );
		dataFetcherThread.start();
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
		dataFetcher.queueRead( new PendingRead( statistic, xMin, xMax, level, scalar ) );
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

		private AbstractLineSegmentModel getModel()
		{
			return AbstractLineSegmentModel.this;
		}

		private void read()
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

	private static class DataFetcher implements Runnable
	{
		private final LinkedHashMap<AbstractLineSegmentModel, PendingRead> queue = new LinkedHashMap<AbstractLineSegmentModel, AbstractLineSegmentModel.PendingRead>();

		@Override
		public void run()
		{
			PendingRead pendingRead = null;

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

							Iterator<PendingRead> iterator = queue.values().iterator();
							pendingRead = iterator.next();
							iterator.remove();
						}
						catch( Exception e )
						{
							e.printStackTrace();
						}
					}
					if( pendingRead != null )
					{
						pendingRead.read();
						SwingUtilities.invokeLater( pendingRead );
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}

		private void queueRead( PendingRead read )
		{
			synchronized( queue )
			{
				queue.put( read.getModel(), read );
				queue.notifyAll();
			}
		}
	}
}
