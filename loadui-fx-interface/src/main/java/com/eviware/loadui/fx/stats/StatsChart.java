package com.eviware.loadui.fx.stats;

import java.awt.Color;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.fx.stats.StatisticsModel.StatisticsInner;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ScheduledExecutor;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.NumericAxis;
import com.jidesoft.chart.axis.TimeAxis;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.range.NumericRange;
import com.jidesoft.range.TimeRange;

/*
 * This model is showing collected data in graph
 */
public class StatsChart extends Chart
{
	static Random random = new Random();
	private UpdateTask updateTask = new UpdateTask();
	private TimeAxis timeAxis = new TimeAxis();
	private Map<String, NumericRange> ranges = new TreeMap<String, NumericRange>();

	private ScheduledFuture<?> future;
	private StatisticsModel model;
	private TimeRange timerange;

	public StatsChart( StatisticsModel model )
	{

		this.model = model;

		future = ScheduledExecutor.instance.scheduleAtFixedRate( updateTask, 1000, 1000, TimeUnit.MILLISECONDS );
		if( future != null )
		{
			System.out.println( "Started " );
		}
		initStatsFromModel();

	}

	private void initStatsFromModel()
	{
		for( StatisticsInner stat : model.getStatistics() )
		{
			NumericRange numRange = new NumericRange();
			NumericAxis numericAxis = new NumericAxis();
			numRange.setMin( 0 );
			numRange.setMax( 1000 );
			ranges.put( stat.getName(), numRange );
			numericAxis.setRange( numRange );
			numericAxis.setVisible( false );
			addChartModel( stat.getName(), numericAxis );
		}

		getYAxis().setVisible( false );
		timerange = new TimeRange();
		timerange.setMin( System.currentTimeMillis() - 60000 );
		timerange.setMax( System.currentTimeMillis() );
		timeAxis.setRange( timerange );
		timeAxis.setVisible( false );
		setXAxis( timeAxis );
	}

	private class UpdateTask implements Runnable
	{
		@Override
		public void run()
		{
			updateChart();
		}

	}

	private void updateChart()
	{
		/*
		 * 1.get a model 
		 * 2. add a point 
		 * 3. check if point is in range, than
		 * increase range
		 */
		ExecutionManager manager = BeanInjector.getBean( ExecutionManager.class );
		try
		{
			if( manager.getCurrentExecution() == null )
			{
				return;
			}
		}
		catch( Throwable t )
		{
			t.printStackTrace();
			return;
		}
		for( StatisticsInner stat : model.getStatistics() )
		{
			try
			{
				DefaultChartModel model = ( DefaultChartModel )getModel( stat.getName() );
				if( model == null )
				{
					System.out.println( "model is null for " + stat.getName() );
					continue;
				}
				if( stat.getValue() == null )
					continue;
				long val = stat.getValue().longValue();
				model.addPoint( System.currentTimeMillis(), val, false );
				if( val >= model.getYRange().maximum() )
				{
					ranges.get( stat.getName() ).setMax( 1.5 * model.getYRange().maximum() );
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		timerange.setMin( System.currentTimeMillis() - 30000 );
		timerange.setMax( System.currentTimeMillis() + 30000 );
	}

	public DefaultChartModel addChartModel( String name, Axis axis )
	{
		if( getModel( name ) != null )
		{
			return ( DefaultChartModel )getModel( name );
		}
		else
		{
			DefaultChartModel newChartModel = new DefaultChartModel( name );
			addYAxis( axis );
			addModel( newChartModel, new ChartStyle( Color.getHSBColor( random.nextFloat(), 1.0F, 1.0F ), false, true ) );
			setModelAxis( newChartModel, axis );

			return newChartModel;
		}
	}

	public void release()
	{
		future.cancel( true );
	}

}
