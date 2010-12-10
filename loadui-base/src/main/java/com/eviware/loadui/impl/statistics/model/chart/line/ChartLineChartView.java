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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;
import com.eviware.loadui.util.StringUtils;

/**
 * ConfigurableLineChartView for a Chart.
 * 
 * @author dain.nilsson
 */
public class ChartLineChartView extends AbstractLineChartView implements ConfigurableLineChartView
{
	private final static String SEGMENTS_ATTRIBUTE = "segments";

	private final LineChartViewProvider provider;
	private final Chart chart;

	public ChartLineChartView( LineChartViewProvider provider, Chart chart )
	{
		super( provider, chart, CHART_PREFIX );

		this.provider = provider;
		this.chart = chart;
		for( String segmentString : StringUtils.deserialize( getAttribute( SEGMENTS_ATTRIBUTE, "" ) ) )
		{
			List<String> parts = StringUtils.deserialize( segmentString );
			ChartLineSegment segment = new ChartLineSegment( chart, parts.get( 0 ), parts.get( 1 ), parts.get( 2 ) );
			putSegment( segment );
			provider.fireSegmentAdded( segment );
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
		ChartLineSegment segment = new ChartLineSegment( chart, variableName, statisticName, source );
		String segmentId = segment.toString();

		if( getSegment( segmentId ) == null )
		{
			putSegment( segment );
			storeSegments();
			provider.fireSegmentAdded( segment );
		}

		return getSegment( segmentId );
	}

	@Override
	public void removeSegment( LineSegment segment )
	{
		if( deleteSegment( segment ) )
		{
			storeSegments();
			provider.fireSegmentRemoved( segment );
		}
	}

	private void storeSegments()
	{
		List<String> segmentsStrings = new ArrayList<String>();
		for( LineSegment lineSegment : getSegments() )
			segmentsStrings.add( lineSegment.toString() );
		setAttribute( SEGMENTS_ATTRIBUTE, StringUtils.serialize( segmentsStrings ) );
	}

	@Override
	protected void segmentAdded( LineSegment segment )
	{
	}

	@Override
	protected void segmentRemoved( LineSegment segment )
	{
	}

	@Override
	public String toString()
	{
		return chart.getStatisticHolder().toString();
	}
}