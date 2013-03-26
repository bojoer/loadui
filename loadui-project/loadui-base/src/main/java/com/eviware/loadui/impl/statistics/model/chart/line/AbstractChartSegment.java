/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.statistics.model.chart.line;

import java.util.Collection;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.impl.property.DelegatingAttributeHolderSupport;

public class AbstractChartSegment implements Segment.Removable
{
	private final ChartLineChartView chartView;
	private final DelegatingAttributeHolderSupport attributeSupport;
	private final String id;
	private boolean isRemoved = false;

	public AbstractChartSegment( ChartLineChartView chart, String id )
	{
		this.chartView = chart;
		this.id = id;

		attributeSupport = new DelegatingAttributeHolderSupport( chart, "_SEGMENT_" + id + "_" );
	}

	public Chart getChart()
	{
		return chartView.getChart();
	}

	public ChartLineChartView getChartView()
	{
		return chartView;
	}

	@Override
	public void setAttribute( String key, String value )
	{
		attributeSupport.setAttribute( key, value );
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		return attributeSupport.getAttribute( key, defaultValue );
	}

	@Override
	public void removeAttribute( String key )
	{
		attributeSupport.removeAttribute( key );
	}

	@Override
	public Collection<String> getAttributes()
	{
		return attributeSupport.getAttributes();
	}

	@Override
	public boolean isRemoved()
	{
		return isRemoved;
	}

	@Override
	public void remove()
	{
		isRemoved = true;
		chartView.removeSegment( this );
	}

	@Override
	public String toString()
	{
		return id;
	}

	@Override
	public String getId()
	{
		return id;
	}

}
