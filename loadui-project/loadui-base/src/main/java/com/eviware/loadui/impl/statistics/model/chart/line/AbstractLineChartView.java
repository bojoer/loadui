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

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.impl.statistics.model.chart.AbstractChartView;
import com.eviware.loadui.util.events.EventSupport;

/**
 * Abstract base class for LineChartView implementations.
 * 
 * @author dain.nilsson
 */
public abstract class AbstractLineChartView extends AbstractChartView implements LineChartView
{
	private final EventSupport eventSupport = new EventSupport();
	private final Map<String, LineSegment> segments = new HashMap<String, LineSegment>();

	public AbstractLineChartView( LineChartViewProvider provider, AttributeHolder attributeDelegate, String prefix )
	{
		super( provider, attributeDelegate, prefix );

		provider.addEventListener( CollectionEvent.class, new ProviderListener() );
	}

	/**
	 * Adds the given LineSegment and fires a CollectionEvent about it.
	 * 
	 * @param segment
	 */
	protected void putSegment( LineSegment segment )
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
	protected LineSegment getSegment( String segmentKey )
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
	protected boolean deleteSegment( LineSegment segment )
	{
		for( Entry<String, LineSegment> entry : segments.entrySet() )
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

	protected abstract void segmentAdded( LineSegment segment );

	protected abstract void segmentRemoved( LineSegment segment );

	@Override
	public Collection<LineSegment> getSegments()
	{
		return Collections.unmodifiableCollection( segments.values() );
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
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

	private class ProviderListener implements EventHandler<CollectionEvent>
	{
		@Override
		public void handleEvent( CollectionEvent event )
		{
			if( LineChartViewProvider.LINE_SEGMENTS.equals( event.getKey() ) )
			{
				LineSegment segment = ( LineSegment )event.getElement();
				if( CollectionEvent.Event.ADDED == event.getEvent() )
					segmentAdded( segment );
				else
					segmentRemoved( segment );
			}
		}
	}
}
