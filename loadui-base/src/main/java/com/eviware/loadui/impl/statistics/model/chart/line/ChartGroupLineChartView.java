/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.statistics.model.chart.line;

import java.util.Set;

import com.eviware.loadui.api.statistics.model.ChartGroup;

/**
 * LineChartView for a ChartGroup. Not configurable.
 * 
 * @author dain.nilsson
 */
public class ChartGroupLineChartView extends AbstractLineChartView
{

	public ChartGroupLineChartView( ChartGroup chartGroup )
	{
		super( chartGroup, CHART_GROUP_PREFIX );
	}

	@Override
	public Set<String> getVariableNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getStatisticNames( String variableName )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getSources( String variableName )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LineSegment addSegment( String variableName, String statisticName, String source )
	{
		throw new UnsupportedOperationException(
				"Cannot add LineSegments to ChartGroupLineChartView, add them to ChartLineChartView instead!" );
	}

	@Override
	public void removeSegment( LineSegment segment )
	{
		throw new UnsupportedOperationException(
				"Cannot remove LineSegments from ChartGroupLineChartView, add them to ChartLineChartView instead!" );
	}
}