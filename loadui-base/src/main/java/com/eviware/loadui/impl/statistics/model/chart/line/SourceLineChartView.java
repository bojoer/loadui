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

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;

/**
 * LineChartView for a Source.
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
		chartGroup.addEventListener( CollectionEvent.class, new ChartGroupListener() );
	}

	private class ChartGroupListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( SEGMENTS.equals( event.getKey() ) )
			{
				LineSegmentImpl segment = ( LineSegmentImpl )event.getElement();
				if( source.equals( segment.getSource() ) )
				{
					if( CollectionEvent.Event.ADDED.equals( event.getEvent() ) )
						putSegment( segment.getSegmentString(), segment );
					else
						deleteSegment( segment );
				}
				else if( StatisticVariable.MAIN_SOURCE.equals( segment.getSource() ) )
				{
					if( CollectionEvent.Event.ADDED.equals( event.getEvent() ) )
					{
						Chart chart = ( Chart )event.getSource();
						LineSegmentImpl sourceSegment = new LineSegmentImpl( chart, segment.getVariableName(),
								segment.getStatisticName(), source );
						putSegment( sourceSegment.getSegmentString(), sourceSegment );
					}
					else
					{
						// TODO: Check if event.getSource() also has a segment for the
						// same
						// Statistic, but with this source. If so, do not delete the
						// segment.
						deleteSegment( getSegment( LineSegmentImpl.createSegmentString( segment.getVariableName(),
								segment.getStatisticName(), source ) ) );
					}
				}
			}
		}
	}
}