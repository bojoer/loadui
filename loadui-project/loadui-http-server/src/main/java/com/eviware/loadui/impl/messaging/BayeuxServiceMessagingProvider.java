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
package com.eviware.loadui.impl.messaging;

import java.util.HashSet;
import java.util.Set;

import org.cometd.Bayeux;
import org.cometd.Client;
import org.cometd.Message;
import org.cometd.RemoveListener;
import org.cometd.server.BayeuxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpointProvider;

public class BayeuxServiceMessagingProvider extends BayeuxService implements MessageEndpointProvider
{
	private static Logger log = LoggerFactory.getLogger( BayeuxServiceMessagingProvider.class );

	private final static String CHANNEL = "/service" + MessageEndpoint.BASE_CHANNEL;
	private final Set<BayeuxServiceMessagingEndpoint> endpoints = new HashSet<BayeuxServiceMessagingEndpoint>();
	private String currentId;

	public BayeuxServiceMessagingProvider( Bayeux bayeux )
	{
		super( bayeux, "messageEndpoint" );
		subscribe( CHANNEL + "/**", "fireMessage" );
	}

	public void fireMessage( Client remote, Message message )
	{
		if( currentId == null )
		{
			currentId = remote.getId();
			remote.addListener( new RemoveListener()
			{
				@Override
				public void removed( String clientId, boolean timeout )
				{
					if( clientId.equals( currentId ) )
						currentId = null;
				}
			} );
		}

		if( remote.getId().equals( currentId ) )
		{
			for( BayeuxServiceMessagingEndpoint endpoint : endpoints )
				endpoint.fireMessage( message.getChannel().substring( CHANNEL.length() ), message.getData() );
		}
		else
		{
			log.info( "Received message from client other than the one currently connected, disconnecting client..." );
			remote.disconnect();
		}
	}

	@Override
	public MessageEndpoint createEndpoint( String url )
	{
		BayeuxServiceMessagingEndpoint endpoint = new BayeuxServiceMessagingEndpoint( this );
		endpoints.add( endpoint );
		return endpoint;
	}

	public void sendMessage( String channel, Object data )
	{
		getBayeux().getChannel( channel, true ).publish( getClient(), data, null );
	}
}
