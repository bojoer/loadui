package com.eviware.loadui.fx.stats;

import java.awt.Color;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.fx.stats.StatisticsModel.Statistics;
import com.eviware.loadui.util.ScheduledExecutor;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.annotation.AutoPositionedLabel;
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
	private UpdateTask updateTask = new UpdateTask();
	private TimeAxis timeAxis = new TimeAxis( new AutoPositionedLabel( "X" ) );
	private NumericAxis numericAxis = new NumericAxis( new AutoPositionedLabel( "Y" ) );

	private ScheduledFuture<?> future;
	private StatisticsModel model;

	public StatsChart( StatisticsModel model )
	{

		this.model = model;

		future = ScheduledExecutor.instance.scheduleAtFixedRate( updateTask, 1000, 1000, TimeUnit.MILLISECONDS );

		initStatsFromModel();

	}

	private void initStatsFromModel()
	{
		for( Statistics stat : model.getStatistics() )
		{
			addChartModel( stat.getName(), stat.getChartStyle() );
		}

		TimeRange range = new TimeRange();
		range.setMin( System.currentTimeMillis() - 60000 );
		range.setMax( System.currentTimeMillis() );
		timeAxis.setRange( range );
		setXAxis( timeAxis );
		NumericRange numRange = new NumericRange();
		numRange.setMin( 0 );
		numRange.setMax( 100 );
		numericAxis.setRange( numRange );
		setYAxis( numericAxis );
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

		for( Statistics stat : model.getStatistics() )
		{
			DefaultChartModel model = ( DefaultChartModel )getModel( stat.getName() );
			model.addPoint( System.currentTimeMillis(), stat.getValue( System.currentTimeMillis() ) );
			if( getYAxis().getRange().maximum() <= model.getYRange().maximum() )
			{
				NumericRange numRange = new NumericRange();
				numRange.setMin( 0 );
				numRange.setMax( 10 * model.getYRange().maximum() );
				numericAxis.setRange( numRange );
				setYAxis( numericAxis );
			}
		}

		TimeRange range = new TimeRange();
		range.setMin( System.currentTimeMillis() - 60000 );
		range.setMax( System.currentTimeMillis() );
		timeAxis.setRange( range );
	}

	public DefaultChartModel addChartModel( String name, ChartStyle style )
	{
		if( getModel( name ) != null )
		{
			return null;
		}
		else
		{
			DefaultChartModel newChartModel = new DefaultChartModel( name );

			addModel( newChartModel, style );
			return newChartModel;
		}
	}

}
