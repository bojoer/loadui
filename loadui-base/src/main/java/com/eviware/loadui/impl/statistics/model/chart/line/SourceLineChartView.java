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
	}

	@Override
	protected void segmentAdded( LineSegmentImpl segment )
	{
		if( source.equals( segment.getSource() ) )
			putSegment( segment.getSegmentString(), segment );
		else if( StatisticVariable.MAIN_SOURCE.equals( segment.getSource() ) )
		{
			LineSegmentImpl sourceSegment = new LineSegmentImpl( segment.getChart(), segment.getVariableName(),
					segment.getStatisticName(), source );
			putSegment( sourceSegment.getSegmentString(), sourceSegment );
		}
	}

	@Override
	protected void segmentRemoved( LineSegmentImpl segment )
	{
		if( source.equals( segment.getSource() ) )
			deleteSegment( segment );
		else if( StatisticVariable.MAIN_SOURCE.equals( segment.getSource() ) )
		{
			// TODO: Check if chart also has a segment for the
			// same
			// Statistic, but with this source. If so, do not delete the
			// segment.
			deleteSegment( getSegment( LineSegmentImpl.createSegmentString( segment.getVariableName(),
					segment.getStatisticName(), source ) ) );
		}
	}
}