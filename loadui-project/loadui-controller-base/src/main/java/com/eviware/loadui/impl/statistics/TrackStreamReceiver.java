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

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.util.MapUtils;
import com.eviware.loadui.util.statistics.store.EntryImpl;

/**
 * Receives Track data from agents and saves it to the current Execution.
 * 
 * @author dain.nilsson
 */
public class TrackStreamReceiver
{
	private static final String CHANNEL = "/" + StatisticsManager.class.getName() + "/execution";

	public final static Logger log = LoggerFactory.getLogger( TrackStreamReceiver.class );

	private final MessageEndpoint endpoint;
	private final AgentDataAggregator aggregator;
	private final ExecutionManager executionManager;

	public TrackStreamReceiver( BroadcastMessageEndpoint endpoint, AgentDataAggregator aggregator,
			ExecutionManager executionManager )
	{
		this.endpoint = endpoint;
		this.aggregator = aggregator;
		this.executionManager = executionManager;

		this.endpoint.addMessageListener( "/" + Statistic.class.getName(), new MessageListener()
		{
			@Override
			@SuppressWarnings( "unchecked" )
			public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
			{
				if( endpoint instanceof AgentItem )
				{
					Map<String, Object> map = ( Map<String, Object> )data;
					AgentItem agent = ( AgentItem )endpoint;
					String executionId = map.remove( "_EXECUTION" ).toString();
					Execution execution = TrackStreamReceiver.this.executionManager.getCurrentExecution();
					int level = ( ( Number )map.remove( "_LEVEL" ) ).intValue();
					int timestamp = ( ( Number )map.remove( "_TIMESTAMP" ) ).intValue();
					String trackId = ( String )map.remove( "_TRACK_ID" );

					EntryImpl entry = new EntryImpl( timestamp, ( Map<String, Number> )data, true );
					if( execution != null && execution.getId().equals( executionId ) )
					{
						TrackStreamReceiver.this.aggregator.update( entry, trackId, agent, level );
					}
					else
					{
						log.warn( "Received Entry: {} for incorrect Execution: {}", entry, executionId );
						if( execution != null )
						{
							log.debug( "Correcting Agent execution: {}, length: {}", execution.getId(), execution.getLength() );
							endpoint.sendMessage(
									CHANNEL,
									MapUtils.build( String.class, Object.class ).put( "START", execution.getId() )
											.put( "LENGTH", execution.getLength() ).get() );
						}
					}
				}
			}
		} );
	}
}
