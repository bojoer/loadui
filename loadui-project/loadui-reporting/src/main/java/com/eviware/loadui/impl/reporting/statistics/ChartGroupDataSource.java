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
package com.eviware.loadui.impl.reporting.statistics;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;

public class ChartGroupDataSource extends JRAbstractBeanDataSource
{
	private final Map<Object, Image> charts;
	private final List<ChartView> chartViews = new ArrayList<>();
	private ChartView currentChartView;
	private Iterator<ChartView> chartViewIterator;

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
		ArrayList<String> sources = new ArrayList<>();
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
			return currentChartView.getLabel();
		else if( fieldName.equals( "chart" ) )
			return charts.get( currentChartView );
		else if( fieldName.equals( "legend" ) )
			return new ChartLegendDataSource( ( LineChartView )currentChartView );
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		while( chartViewIterator.hasNext() )
		{
			currentChartView = chartViewIterator.next();
			// If there is no chart stored for this ChartView, skip it.
			if( charts.containsKey( currentChartView ) )
				return true;
		}
		return false;
	}
}
