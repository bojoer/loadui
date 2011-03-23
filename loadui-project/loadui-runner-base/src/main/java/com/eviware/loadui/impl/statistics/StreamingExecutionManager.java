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
package com.eviware.loadui.impl.statistics;

import java.awt.Image;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionListener;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;
import com.eviware.loadui.util.statistics.store.ExecutionChangeSupport;

public class StreamingExecutionManager implements ExecutionManager, Releasable
{
	public static final Logger log = LoggerFactory.getLogger( StreamingExecutionManager.class );

	private static final String STATISTICS_CHANNEL = "/" + Statistic.class.getName();

	private final EventSupport eventSupport = new EventSupport();
	private final Map<String, TrackDescriptor> trackDescriptors = new HashMap<String, TrackDescriptor>();
	private final Map<String, Track> trackMap = new HashMap<String, Track>();
	private final Set<MessageEndpoint> endpoints = new HashSet<MessageEndpoint>();
	private final ExecutionChangeSupport ecs = new ExecutionChangeSupport();

	@Override
	public Execution getCurrentExecution()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Execution startExecution( String executionId, long startTime )
	{
		return startExecution( executionId, startTime, null );
	}

	@Override
	public Execution startExecution( String executionId, long startTime, String label )
	{
		return startExecution( executionId, startTime, label, "Execution_" + String.valueOf( startTime ) );
	}

	@Override
	public Execution startExecution( String executionId, long startTime, String label, String fileName )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void pauseExecution()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void stopExecution()
	{
		throw new UnsupportedOperationException();
	}

	public void addEndpoint( MessageEndpoint endpoint )
	{
		log.debug( "Added Endpoint: {}", endpoint );
		endpoints.add( endpoint );
	}

	public void removeEndpoint( MessageEndpoint endpoint )
	{
		endpoints.remove( endpoint );
	}

	@Override
	public void removeAllExecutionListeners()
	{
		ecs.removeAllExecutionListeners();
	}

	@Override
	public void addExecutionListener( ExecutionListener el )
	{
		ecs.addExecutionListener( el );
	}

	@Override
	public void removeExecutionListener( ExecutionListener el )
	{
		ecs.removeExecutionListener( el );
	}

	@Override
	public void registerTrackDescriptor( TrackDescriptor trackDescriptor )
	{
		trackDescriptors.put( trackDescriptor.getId(), trackDescriptor );
		ecs.fireTrackRegistered( trackDescriptor );
	}

	@Override
	public void unregisterTrackDescriptor( String trackId )
	{
		ecs.fireTrackUnregistered( trackDescriptors.remove( trackId ) );
	}

	@Override
	public Track getTrack( String trackId )
	{
		return trackMap.get( trackId );
	}

	@Override
	public Collection<String> getTrackIds()
	{
		return trackMap.keySet();
	}

	@Override
	public void writeEntry( String trackId, Entry entry, String source )
	{
		writeEntry( trackId, entry, source, 0 );
	}

	@Override
	public void writeEntry( String trackId, Entry entry, String source, int interpolationLevel )
	{
		Map<String, Object> data = new HashMap<String, Object>();
		for( String key : entry.getNames() )
			data.put( key, entry.getValue( key ) );
		data.put( "_TIMESTAMP", System.currentTimeMillis() - entry.getTimestamp() );
		data.put( "_TRACK_ID", trackId );
		data.put( "_LEVEL", interpolationLevel );

		for( MessageEndpoint endpoint : endpoints )
			endpoint.sendMessage( STATISTICS_CHANNEL, data );
	}

	@Override
	public Entry getLastEntry( String trackId, String source )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entry getLastEntry( String trackId, String source, int interpolationLevel )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Execution> getExecutions()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Execution getExecution( String executionId )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDBBaseDir()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public State getState()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void release()
	{
		ReleasableUtils.release( eventSupport );
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
}
