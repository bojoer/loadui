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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionListener;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.util.statistics.store.ExecutionChangeSupport;

public class StreamingExecutionManager implements ExecutionManager
{
	public static final Logger log = LoggerFactory.getLogger( StreamingExecutionManager.class );

	private static final String STATISTICS_CHANNEL = "/" + Statistic.class.getName();
	private static final String EXECUTION_CHANNEL = "/" + StatisticsManager.class.getName() + "/execution";

	private final Map<String, TrackDescriptor> trackDescriptors = new HashMap<String, TrackDescriptor>();
	private final Map<String, Track> trackMap = new HashMap<String, Track>();
	private final Set<MessageEndpoint> endpoints = new HashSet<MessageEndpoint>();
	private final ExecutionChangeSupport ecs = new ExecutionChangeSupport();
	private final ExecutionMessageListener executionMessageListener = new ExecutionMessageListener();
	private State executionState = State.STOPPED;
	private Execution currentExecution;

	@Override
	public Execution getCurrentExecution()
	{
		return currentExecution;
	}

	@Override
	public Execution startExecution( String executionId, long startTime )
	{
		// unpause if paused otherwise try to create new
		if( executionState == State.PAUSED )
		{
			executionState = State.STARTED;
			ecs.fireExecutionStarted( State.PAUSED );
			log.debug( "State changed: PAUSED -> STARTED" );
			return currentExecution;
		}

		currentExecution = new ExecutionImpl( executionId, startTime );

		executionState = State.STARTED;
		ecs.fireExecutionStarted( State.STOPPED );
		log.debug( "State changed: STOPPED -> STARTED" );

		return currentExecution;
	}

	@Override
	public void pauseExecution()
	{
		// if started and not paused ( can not pause something that is not started
		// )
		if( executionState == State.STARTED )
		{
			executionState = State.PAUSED;
			ecs.fireExecutionPaused( State.STARTED );
		}
	}

	@Override
	public void stopExecution()
	{
		// execution can be stopped only if started or paused previously
		if( executionState == State.STARTED || executionState == State.PAUSED )
		{
			State oldState = executionState;
			executionState = State.STOPPED;
			ecs.fireExecutionStopped( oldState );
		}
	}

	public void addEndpoint( MessageEndpoint endpoint )
	{
		log.debug( "Added Endpoint: {}", endpoint );
		endpoints.add( endpoint );
		endpoint.addMessageListener( EXECUTION_CHANNEL, executionMessageListener );
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
		data.put( "_TIMESTAMP", entry.getTimestamp() );
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
	public Collection<String> getExecutionNames()
	{
		return currentExecution == null ? Collections.<String> emptyList() : Collections.singletonList( currentExecution
				.getId() );
	}

	@Override
	public Execution getExecution( String executionId )
	{
		if( currentExecution == null || !currentExecution.getId().equals( executionId ) )
			return null;

		return currentExecution;
	}

	@Override
	public State getState()
	{
		return executionState;
	}

	private class ExecutionMessageListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			log.debug( "Received from controller : " + data.toString() );
			String message = data.toString();
			if( message.startsWith( "execution" ) )
				startExecution( data.toString(), System.currentTimeMillis() );
			else if( message.startsWith( "pause" ) )
				pauseExecution();
			else if( message.startsWith( "stop" ) )
				stopExecution();
			else if( message.startsWith( "unpause" ) )
				startExecution( data.toString(), System.currentTimeMillis() );
		}
	}

	private class ExecutionImpl implements Execution
	{
		private final String id;
		private String label;
		private final long startTime;

		public ExecutionImpl( String id, long startTime )
		{
			this.id = id;
			this.startTime = startTime;
		}

		@Override
		public String getId()
		{
			return id;
		}

		@Override
		public long getStartTime()
		{
			return startTime;
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
		public void delete()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isArchived()
		{
			return false;
		}

		@Override
		public void archive()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getLabel()
		{
			return label;
		}

		@Override
		public void setLabel( String label )
		{
			this.label = label;
		}
	}
}
