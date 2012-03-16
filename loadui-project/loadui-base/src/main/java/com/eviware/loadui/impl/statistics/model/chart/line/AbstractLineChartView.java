/*
 * Copyright 2011 SmartBear Software
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

import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.statistics.model.chart.AbstractChartView;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;
import com.google.common.collect.ImmutableList;

/**
 * Abstract base class for LineChartView implementations.
 * 
 * @author dain.nilsson
 */
public abstract class AbstractLineChartView extends AbstractChartView implements LineChartView, Releasable
{
	private final EventSupport eventSupport = new EventSupport( this );
	private final Map<String, Segment> segments = new HashMap<String, Segment>();

	public AbstractLineChartView( LineChartViewProvider provider, AttributeHolder attributeDelegate, String prefix )
	{
		super( provider, attributeDelegate, prefix );

		provider.addEventListener( CollectionEvent.class, new ProviderListener() );
	}

	/**
	 * Adds the given Segment and fires a CollectionEvent about it.
	 * 
	 * @param segment
	 */
	protected void putSegment( Segment segment )
	{
		segments.put( segment.toString(), segment );
		fireEvent( new CollectionEvent( this, SEGMENTS, CollectionEvent.Event.ADDED, segment ) );
	}

	/**
	 * Returns a contained LineSegment for the given key.
	 * 
	 * @param segmentKey
	 * @return
	 */
	protected Segment getSegment( String segmentKey )
	{
		return segments.get( segmentKey );
	}

	/**
	 * Removes a contained LineSegment and fires a CollectionEvent, if
	 * successful. Returns the status of the operation.
	 * 
	 * @param segment
	 * @return
	 */
	protected boolean deleteSegment( Segment segment )
	{
		for( Entry<String, Segment> entry : segments.entrySet() )
		{
			if( entry.getValue().equals( segment ) )
			{
				segments.remove( entry.getKey() );
				for( String attr : segment.getAttributes() )
					segment.removeAttribute( attr );
				fireEvent( new CollectionEvent( this, SEGMENTS, CollectionEvent.Event.REMOVED, segment ) );
				return true;
			}
		}

		return false;
	}

	protected abstract void segmentAdded( Segment segment );

	protected abstract void segmentRemoved( Segment segment );

	@Override
	public Collection<Segment> getSegments()
	{
		return ImmutableList.copyOf( segments.values() );
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}

	@Override
	public void release()
	{
		ReleasableUtils.release( eventSupport );
	}

	private class ProviderListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( LineChartViewProvider.LINE_SEGMENTS.equals( event.getKey() ) )
			{
				Segment segment = ( Segment )event.getElement();
				if( CollectionEvent.Event.ADDED == event.getEvent() )
					segmentAdded( segment );
				else
					segmentRemoved( segment );
			}
		}
	}
}
