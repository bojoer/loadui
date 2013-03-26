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

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;

/**
 * LineChartView for a ChartGroup.
 * 
 * @author dain.nilsson
 */
public class ChartGroupLineChartView extends AbstractLineChartView
{
	private final ChartGroup chartGroup;

	public ChartGroupLineChartView( LineChartViewProvider provider, ChartGroup chartGroup )
	{
		super( provider, chartGroup, CHART_GROUP_PREFIX );

		this.chartGroup = chartGroup;
	}

	@Override
	protected void segmentAdded( Segment segment )
	{
		if( segment instanceof AbstractChartSegment )
			putSegment( segment );
	}

	@Override
	protected void segmentRemoved( Segment segment )
	{
		if( segment instanceof AbstractChartSegment )
			deleteSegment( segment );
	}

	@Override
	public String toString()
	{
		return chartGroup.getLabel();
	}

	@Override
	public String getLabel()
	{
		return "Total/together";
	}
}
