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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.util.StringUtils;

/**
 * LineChartView for a Chart.
 * 
 * @author dain.nilsson
 */
public class ChartLineChartView extends AbstractLineChartView
{
	private final static String SEGMENTS_ATTRIBUTE = "segments";

	private final Chart chart;

	public ChartLineChartView( Chart chart )
	{
		super( chart.getChartGroup(), CHART_PREFIX );

		this.chart = chart;
		for( String segmentString : StringUtils.deserialize( getAttribute( SEGMENTS_ATTRIBUTE, "" ) ) )
		{
			List<String> parts = StringUtils.deserialize( segmentString );
			putSegment( segmentString, new LineSegmentImpl( chart, parts.get( 0 ), parts.get( 1 ), parts.get( 2 ) ) );
		}
	}

	@Override
	public Set<String> getVariableNames()
	{
		return chart.getStatisticHolder().getStatisticVariableNames();
	}

	@Override
	public Set<String> getStatisticNames( String variableName )
	{
		return chart.getStatisticHolder().getStatisticVariable( variableName ).getStatisticNames();
	}

	@Override
	public Set<String> getSources( String variableName )
	{
		return chart.getStatisticHolder().getStatisticVariable( variableName ).getSources();
	}

	@Override
	public LineSegment addSegment( String variableName, String statisticName, String source )
	{
		String segmentString = StringUtils.serialize( Arrays.asList( variableName, statisticName, source ) );
		if( getSegment( segmentString ) == null )
		{
			LineSegment segment = new LineSegmentImpl( chart, variableName, statisticName, source );
			putSegment( segmentString, segment );
			setAttribute( SEGMENTS_ATTRIBUTE, StringUtils.serialize( segmentKeySet() ) );
			fireEvent( new CollectionEvent( this, SEGMENTS, CollectionEvent.Event.ADDED, segment ) );
		}

		return getSegment( segmentString );
	}

	@Override
	public void removeSegment( LineSegment segment )
	{
		if( deleteSegment( segment ) )
			setAttribute( SEGMENTS_ATTRIBUTE, StringUtils.serialize( segmentKeySet() ) );
	}
}