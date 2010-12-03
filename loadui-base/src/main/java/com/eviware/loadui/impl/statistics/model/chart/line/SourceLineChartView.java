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

import java.util.Collections;
import java.util.Set;

import com.eviware.loadui.api.statistics.model.ChartGroup;

/**
 * LineChartView for a Source. Not configurable.
 * 
 * @author dain.nilsson
 */
public class SourceLineChartView extends AbstractLineChartView
{
	private final String source;

	public SourceLineChartView( ChartGroup chartGroup, String source )
	{
		super( chartGroup, SOURCE_PREFIX + source );

		this.source = source;
		// TODO: Add a listener to the ChartGroup to listen for added/removed
		// LineSegments, and add/remove them to the SourceLineChartView if needed.
	}

	@Override
	public Set<String> getVariableNames()
	{
		// TODO: Collect all StatisticHolder variables.
		return null;
	}

	@Override
	public Set<String> getStatisticNames( String variableName )
	{
		// TODO Collect all Statistic names for each StatisticVariable for the
		// given name, for each StatisticHolder.
		return null;
	}

	@Override
	public Set<String> getSources( String variableName )
	{
		return Collections.singleton( source );
	}

	@Override
	public LineSegment addSegment( String variableName, String statisticName, String source )
	{
		throw new UnsupportedOperationException(
				"Cannot add LineSegments to SourceLineChartView, add them to ChartLineChartView instead!" );
	}

	@Override
	public void removeSegment( LineSegment segment )
	{
		throw new UnsupportedOperationException(
				"Cannot remove LineSegments from SourceLineChartView, remove them from ChartLineChartView instead!" );
	}
}