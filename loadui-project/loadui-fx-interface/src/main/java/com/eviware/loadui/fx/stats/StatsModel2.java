package com.eviware.loadui.fx.stats;

import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.api.model.ProjectItem;
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
public class StatsModel2 extends Chart
{
	private ProjectItem project;
	private UpdateTask updateTask = new UpdateTask();
	private TimeAxis timeAxis = new TimeAxis( new AutoPositionedLabel("X") );
	private NumericAxis numericAxis = new NumericAxis( new AutoPositionedLabel("Y") );
	
	private ScheduledFuture<?> future;
	
	public StatsModel2( ProjectItem project )
	{
		this.project = project;
		
		future = ScheduledExecutor.instance.scheduleAtFixedRate( updateTask, 1000, 1000, TimeUnit.MILLISECONDS );
		
		initStats();
	}
	
	private void initStats()
	{
		ChartStyle styleA = new ChartStyle(Color.blue, false, true);
		ChartStyle styleB = new ChartStyle(Color.red, false, true);
		

		addChartModel( "assertion", styleA );
		addChartModel( "failures", styleB );
		
		TimeRange range =  new TimeRange();
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
		 * 3. check if point is in range, than increase range
		 */
		DefaultChartModel modelA = ( DefaultChartModel )getModel( "assertion" );
		modelA.addPoint( System.currentTimeMillis(), project.getCounter( ProjectItem.ASSERTION_COUNTER ).get() );
		if ( getYAxis().getRange().maximum() <=  project.getCounter( ProjectItem.ASSERTION_COUNTER ).get() ) {
			NumericRange numRange = new NumericRange();
			numRange.setMin( 0 );
			numRange.setMax( 10*project.getCounter( ProjectItem.ASSERTION_COUNTER ).get() );
			numericAxis.setRange( numRange );
			setYAxis( numericAxis );
		}
		
		modelA = ( DefaultChartModel )getModel( "failures" );
		modelA.addPoint( System.currentTimeMillis(), project.getCounter( ProjectItem.FAILURE_COUNTER ).get() );
		if ( getYAxis().getRange().maximum() <=  project.getCounter( ProjectItem.FAILURE_COUNTER ).get() ) {
			NumericRange numRange = new NumericRange();
			numRange.setMin( 0 );
			numRange.setMax( 10*project.getCounter( ProjectItem.FAILURE_COUNTER ).get() );
			numericAxis.setRange( numRange );
			setYAxis( numericAxis );
		}
		TimeRange range =  new TimeRange();
		range.setMin( System.currentTimeMillis() - 60000 );
		range.setMax( System.currentTimeMillis() );
		timeAxis.setRange( range );
	}
	
	public DefaultChartModel addChartModel(String name, ChartStyle style) {
		if( getModel(name) != null ) {
			return null;
		} else {
			DefaultChartModel newChartModel = new DefaultChartModel(name);
//			newChartModel.addChartModelListener( new ChartModelListener()
//			{
//				
//				@Override
//				public void chartModelChanged()
//				{
//					newChartModel.getPointCount()
//					
//				}
//			});
			addModel( newChartModel, style );
			return newChartModel;
		}
	}
		
}
