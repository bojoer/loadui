package com.eviware.loadui.util.reporting.datasources.statistics;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.store.Execution;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class ChartGroupDataSource extends JRAbstractBeanDataSource
{
	private final Execution execution;
	private final ChartGroup chartGroup;
	private final Map<Object, Image> charts;
	private final ArrayList<ChartView> chartViews = new ArrayList<ChartView>();
	private Iterator<ChartView> chartViewIterator;

	public ChartGroupDataSource( Execution execution, ChartGroup chartGroup, Map<Object, Image> charts )
	{
		super( true );

		this.execution = execution;
		this.chartGroup = chartGroup;
		this.charts = charts;

		chartViews.add( chartGroup.getChartView() );
		for( Chart chart : chartGroup.getChildren() )
		{
			ChartView chartView = chartGroup.getChartViewForChart( chart );
			if( charts.containsKey( chartView ) )
				chartViews.add( chartView );
		}
		ArrayList<String> sources = new ArrayList<String>();
		sources.addAll( chartGroup.getSources() );
		Collections.sort( sources );
		for( String source : sources )
		{
			ChartView chartView = chartGroup.getChartViewForSource( source );
			if( charts.containsKey( chartView ) )
				chartViews.add( chartView );
		}

		chartViewIterator = chartViews.iterator();
	}

	@Override
	public void moveFirst() throws JRException
	{
		chartViewIterator = chartViews.iterator();
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		if( field.getName().equals( "chart" ) )
			return charts.get( chartViewIterator.next() );
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		return chartViewIterator.hasNext();
	}
}
