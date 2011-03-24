/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.impl.reporting.statistics;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class ChartGroupDataSource extends JRAbstractBeanDataSource
{
	private final Map<Object, Image> charts;
	private final ArrayList<ChartView> chartViews = new ArrayList<ChartView>();
	private Iterator<ChartView> chartViewIterator;
	private ChartView chartView;

	public ChartGroupDataSource( ChartGroup chartGroup, Map<Object, Image> charts )
	{
		super( true );

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
		String fieldName = field.getName();
		if( fieldName.equals( "chartName" ) )
			return chartView.getLabel();
		else if( fieldName.equals( "chart" ) )
			return charts.get( chartView );
		else if( fieldName.equals( "legend" ) )
			return new ChartLegendDataSource( ( LineChartView )chartView );
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		while( chartViewIterator.hasNext() )
		{
			chartView = chartViewIterator.next();
			// If there is no chart stored for this ChartView, skip it.
			if( charts.containsKey( chartView ) )
				return true;
		}
		return false;
	}
}
