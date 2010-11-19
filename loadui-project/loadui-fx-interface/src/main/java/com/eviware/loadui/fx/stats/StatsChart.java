package com.eviware.loadui.fx.stats;

import java.awt.Color;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.fx.stats.StatisticsModel.StatisticsInner;
import com.eviware.loadui.impl.statistics.store.ExecutionManagerImpl;
import com.eviware.loadui.impl.statistics.store.H2ExecutionManager;
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
		//This is not working.
		/*
		 * This is giving this:
		  java.lang.NoClassDefFoundError: com/eviware/loadui/impl/statistics/store/H2ExecutionManager
		  at com.eviware.loadui.fx.stats.StatsChart.updateChart(StatsChart.java:95)
		  at com.eviware.loadui.fx.stats.StatsChart.access$100(StatsChart.java:26)
			at com.eviware.loadui.fx.stats.StatsChart$UpdateTask.run(StatsChart.java:79)
			at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:441)
			at java.util.concurrent.FutureTask$Sync.innerRunAndReset(FutureTask.java:317)
			at java.util.concurrent.FutureTask.runAndReset(FutureTask.java:150)
			at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$101(ScheduledThreadPoolExecutor.java:98)
			at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.runPeriodic(ScheduledThreadPoolExecutor.java:180)
			at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:204)
			at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
			at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
			at java.lang.Thread.run(Thread.java:662)
		Caused by: java.lang.ClassNotFoundException: com.eviware.loadui.impl.statistics.store.H2ExecutionManager not found by com.eviware.loadui.fx-interface [25]
			at org.apache.felix.framework.ModuleImpl.findClassOrResourceByDelegation(ModuleImpl.java:787)
			at org.apache.felix.framework.ModuleImpl.access$400(ModuleImpl.java:71)
			at org.apache.felix.framework.ModuleImpl$ModuleClassLoader.loadClass(ModuleImpl.java:1768)
			at java.lang.ClassLoader.loadClass(ClassLoader.java:248)
			... 12 more
		 */
//		try
//		{
//			if( H2ExecutionManager.getInstance().getCurrentExecution() == null )
//			{
//				System.out.println( "No current execution" );
//				return;
//			}
//			else
//			{
//				System.out.println( "there is execution" );
//			}
//		}
//		catch( Throwable t )
//		{
//			t.printStackTrace();
//			return;
//		}
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
