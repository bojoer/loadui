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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.chart.ConfigurableLineChartView;
import com.eviware.loadui.api.statistics.model.chart.DeletableChartView;
import com.eviware.loadui.util.StringUtils;

/**
 * ConfigurableLineChartView for a Chart.
 * 
 * @author dain.nilsson
 */
public class ChartLineChartView extends AbstractLineChartView implements ConfigurableLineChartView, DeletableChartView
{
	public static Logger log = LoggerFactory.getLogger( ChartLineChartView.class );

	private static final String NULL = new String( "null" );

	private final static String SEGMENTS_ATTRIBUTE = "segments";

	private final LineChartViewProvider provider;
	private final Chart chart;
	private boolean released = false;

	private StatisticVariableListener statisticVariableListener = new StatisticVariableListener();

	public ChartLineChartView( LineChartViewProvider provider, Chart chart )
	{
		super( provider, chart, CHART_PREFIX );

		this.provider = provider;
		this.chart = chart;
		for( String segmentString : StringUtils.deserialize( getAttribute( SEGMENTS_ATTRIBUTE, "" ) ) )
		{
			List<String> parts = StringUtils.deserialize( segmentString );
			ChartLineSegment segment = new ChartLineSegment( this, parts.get( 1 ), parts.get( 2 ), parts.get( 3 ) );
			putSegment( segment );
			provider.fireSegmentAdded( segment );
		}
		chart.addEventListener( BaseEvent.class, new ReleaseListener() );
		chart.getStatisticHolder().addEventListener( CollectionEvent.class, statisticVariableListener );
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
	public LineSegment.Removable addSegment( String variableName, String statisticName, String source )
	{
		ChartLineSegment segment = new ChartLineSegment( this, variableName, statisticName, source );
		String segmentId = segment.toString();

		if( getSegment( segmentId ) == null )
		{
			putSegment( segment );
			storeSegments();
			provider.fireSegmentAdded( segment );
		}

		return ( ChartLineSegment )getSegment( segmentId );
	}

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
		if( !released )
		{
			List<String> segmentsStrings = new ArrayList<String>();
			for( LineSegment lineSegment : getSegments() )
				segmentsStrings.add( lineSegment.toString() );
			setAttribute( SEGMENTS_ATTRIBUTE, StringUtils.serialize( segmentsStrings ) );
		}
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
	public void delete()
	{
		chart.delete();
	}

	@Override
	public String toString()
	{
		return chart.getStatisticHolder().getLabel();
	}

	@Override
	public String getLabel()
	{
		return chart.getStatisticHolder().getLabel();
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		String value = super.getAttribute( key, NULL );
		return value == NULL ? getChartGroup().getChartView().getAttribute( key, defaultValue ) : value;
	}

	public Chart getChart()
	{
		return chart;
	}

	private class StatisticVariableListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( event.getEvent() == CollectionEvent.Event.REMOVED && event.getElement() instanceof StatisticVariable )
			{
				StatisticVariable removedElement = ( StatisticVariable )event.getElement();

				Set<LineSegment> lineSegments = new HashSet<LineSegment>( provider.getSegments() );
				for( LineSegment lineSegment : lineSegments )
					if( lineSegment.getStatistic().getStatisticVariable().equals( removedElement ) )
						removeSegment( lineSegment );
			}
		}
	}

	private class ReleaseListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( Releasable.RELEASED.equals( event.getKey() ) )
			{
				released = true;
				for( LineSegment segment : new ArrayList<LineSegment>( getSegments() ) )
					removeSegment( segment );
				chart.getStatisticHolder().removeEventListener( CollectionEvent.class, statisticVariableListener );
			}
		}
	}
}