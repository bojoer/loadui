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
package com.eviware.loadui.impl.statistics.model.chart.line;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.ChartGroup;

/**
 * LineChartView for a Source.
 * 
 * @author dain.nilsson
 */
public class SourceLineChartView extends AbstractLineChartView
{
	private final String source;

	public SourceLineChartView( LineChartViewProvider provider, ChartGroup chartGroup, String source )
	{
		super( provider, chartGroup, SOURCE_PREFIX + source );

		this.source = source;

		for( LineSegment segment : provider.getSegments() )
			segmentAdded( segment );
	}

	@Override
	protected void segmentAdded( LineSegment segment )
	{
		// This adds any Segment for this source, and adds a
		// SourceLineSegment for any Segment for the main source, unless such a
		// Segment already exists.

		// TODO: Think this through a bit more, what happens when segments are
		// added/removed, does this stay in sync?
		if( segment instanceof ChartLineSegment )
		{
			ChartLineSegment chartSegment = ( ChartLineSegment )segment;
			if( source.equals( chartSegment.getSource() ) )
			{
				// TODO: Check if we already have a segment for the main source for
				// this Statistic and if so remove it.
				putSegment( segment );
			}
			else if( StatisticVariable.MAIN_SOURCE.equals( chartSegment.getSource() )
					&& getSegment( chartSegment.toString() ) == null )
			{
				SourceLineSegment sourceSegment = new SourceLineSegment( chartSegment, source );
				putSegment( sourceSegment );
			}
		}
	}

	@Override
	protected void segmentRemoved( LineSegment segment )
	{
		if( segment instanceof ChartLineSegment )
		{
			ChartLineSegment chartSegment = ( ChartLineSegment )segment;
			LineSegment existingSegment = getSegment( chartSegment.toString() );
			if( existingSegment != null )
				deleteSegment( getSegment( chartSegment.toString() ) );
		}
	}

	@Override
	public String toString()
	{
		return source;
	}
}