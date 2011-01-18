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
package com.eviware.loadui.impl.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.ExecutionManager;

/**
 * Periodically reads the current value of each Track in the current Execution,
 * and sends this data to the controller.
 * 
 * @author dain.nilsson
 */
public class TrackStreamer
{
	private static final String CHANNEL = "/" + Statistic.class.getName();
	private static final String EXECUTION_CHANNEL = "/" + StatisticsManager.class.getName() + "/execution";
	private final static Logger log = LoggerFactory.getLogger( TrackStreamer.class );

	private final ExecutionManager manager;
	private final ScheduledExecutorService executor;
	private final ScheduledFuture<?> future;
	private final Set<MessageEndpoint> endpoints = new HashSet<MessageEndpoint>();
	private final ExecutionListener executionListener = new ExecutionListener();

	public TrackStreamer( ScheduledExecutorService scheduledExecutorService, ExecutionManager executionManager )
	{
		manager = executionManager;
		executor = scheduledExecutorService;

		future = executor.scheduleAtFixedRate( new StreamTask(), 1, 1, TimeUnit.SECONDS );
	}

	public void release()
	{
		future.cancel( true );
	}

	public void addEndpoint( MessageEndpoint endpoint )
	{
		endpoints.add( endpoint );
		endpoint.addMessageListener( EXECUTION_CHANNEL, executionListener );
	}

	public void removeEndpoint( MessageEndpoint endpoint )
	{
		endpoints.remove( endpoint );
	}

	private class ExecutionListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			log.debug( "Received from controller : " + data.toString() );
			String message = data.toString();
			if( message.startsWith( "execution" ) )
				manager.startExecution( data.toString(), System.currentTimeMillis() );
			else if( message.startsWith( "pause" ) )
				manager.pauseExecution();
			else if( message.startsWith( "stop" ) )
				manager.stopExecution();
			else if( message.startsWith( "unpause" ) )
				manager.startExecution( data.toString(), System.currentTimeMillis() );
		}
	}

	private class StreamTask implements Runnable
	{
		private Set<Entry> lastEntries;

		@Override
		public void run()
		{
			Map<String, Map<String, Number>> currentData = new HashMap<String, Map<String, Number>>();
			Set<Entry> currentEntries = new HashSet<Entry>();
			for( String trackId : manager.getTrackIds() )
			{
				Entry entry = manager.getLastEntry( trackId, StatisticVariable.MAIN_SOURCE );
				if( entry != null )
				{
					currentEntries.add( entry );
					if( !lastEntries.contains( entry ) )
					{
						Map<String, Number> entryData = new HashMap<String, Number>();
						for( String key : entry.getNames() )
							entryData.put( key, entry.getValue( key ) );
						currentData.put( entry.getTimestamp() + ":" + trackId, entryData );
					}
				}
			}

			lastEntries = currentEntries;

			if( !currentData.isEmpty() )
			{
				log.debug( "Sending Track data: {}", currentData );
				for( MessageEndpoint endpoint : endpoints )
					endpoint.sendMessage( CHANNEL, currentData );
			}
		}
	}
}