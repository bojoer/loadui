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
	private static final String EXECUTION_CHANNEL = "/" + StatisticsManager.class.getName() + "/execution";

	private final EventSupport eventSupport = new EventSupport();
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
		return startExecution( executionId, startTime, null );
	}

	@Override
	public Execution startExecution( String executionId, long startTime, String label )
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
	public String getDBBaseDir()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public State getState()
	{
		return executionState;
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

	private class ExecutionMessageListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			String message = data.toString();
			log.debug( "Received from controller : " + message );
			if( message.startsWith( "pause" ) )
				pauseExecution();
			else if( message.startsWith( "stop" ) )
				stopExecution();
			else if( message.startsWith( "unpause" ) )
				startExecution( message, System.currentTimeMillis() );
			else
				startExecution( message, System.currentTimeMillis() );
		}
	}

	private class ExecutionImpl implements Execution
	{
		private final String id;
		private String label;
		private final long startTime;
		private final Properties attributes = new Properties();

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

		@Override
		public long getLength()
		{
			return 0;
		}

		@Override
		public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clearEventListeners()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void fireEvent( EventObject event )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public File getSummaryReport()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Image getIcon()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setIcon( Image image )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setAttribute( String key, String value )
		{
			attributes.setProperty( key, value );
		}

		@Override
		public String getAttribute( String key, String defaultValue )
		{
			return attributes.getProperty( key, defaultValue );
		}

		@Override
		public void removeAttribute( String key )
		{
			attributes.remove( key );
		}

		@Override
		public Collection<String> getAttributes()
		{
			return attributes.stringPropertyNames();
		}
	}
}
