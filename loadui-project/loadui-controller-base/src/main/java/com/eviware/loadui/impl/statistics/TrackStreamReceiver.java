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

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.util.statistics.store.EntryImpl;

/**
 * Receives Track data from agents and saves it to the current Execution.
 * 
 * @author dain.nilsson
 */
public class TrackStreamReceiver
{
	private final static Logger log = LoggerFactory.getLogger( TrackStreamReceiver.class );

	private final ExecutionManager manager;
	private final MessageEndpoint endpoint;

	public TrackStreamReceiver( ExecutionManager executionManager, BroadcastMessageEndpoint endpoint )
	{
		this.manager = executionManager;
		this.endpoint = endpoint;

		endpoint.addMessageListener( "/" + Statistic.class.getName(), new MessageListener()
		{
			@Override
			public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
			{
				log.debug( "Received Track data from {}: {}", endpoint, data );
				if( endpoint instanceof AgentItem )
				{
					AgentItem agent = ( AgentItem )endpoint;
					@SuppressWarnings( "unchecked" )
					Map<String, Map<String, Number>> currentData = ( Map<String, Map<String, Number>> )data;
					for( Entry<String, Map<String, Number>> entry : currentData.entrySet() )
					{
						String[] parts = entry.getKey().split( ":", 2 );
						int time = Integer.parseInt( parts[0] );
						manager.writeEntry( parts[1], new EntryImpl( time, entry.getValue(), true ), agent.getLabel() );
					}
				}
			}
		} );
	}
}
