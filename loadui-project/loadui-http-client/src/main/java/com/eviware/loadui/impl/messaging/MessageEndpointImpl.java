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

import org.cometd.Client;
import org.cometd.Message;
import org.cometd.client.BayeuxClient;
import org.eclipse.jetty.client.HttpClient;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.util.messaging.ChannelRoutingSupport;

public class MessageEndpointImpl implements MessageEndpoint
{
	private final BayeuxClient client;
	private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport( this );
	private final Set<ConnectionListener> connectionListeners = new HashSet<ConnectionListener>();
	private boolean connected = false;

	public MessageEndpointImpl( String url, HttpClient httpClient )
	{
		try
		{
			client = new BayeuxClient( httpClient, url )
			{
				@Override
				protected void metaConnect( boolean success, Message message )
				{
					super.metaConnect( success, message );
					if( connected != success )
					{
						connected = success;
						for( ConnectionListener listener : connectionListeners )
							listener.handleConnectionChange( MessageEndpointImpl.this, connected );
						if( connected )
							client.subscribe( BASE_CHANNEL + "/**" );
						sendMessage( "", "" );
					}
				}
			};
			client.addListener( new org.cometd.MessageListener()
			{
				@Override
				public void deliver( Client sender, Client receiver, Message message )
				{
					String channel = message.getChannel();
					if( channel.startsWith( BASE_CHANNEL ) && message.getData() != null )
						routingSupport.fireMessage( channel.substring( BASE_CHANNEL.length() ), message.getData() );
				}
			} );
			// client.start();
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public void addMessageListener( String channel, MessageListener listener )
	{
		routingSupport.addMessageListener( channel, listener );
	}

	@Override
	public void removeMessageListener( MessageListener listener )
	{
		routingSupport.removeMessageListener( listener );
	}

	@Override
	public void sendMessage( String channel, Object data )
	{
		client.publish( "/service" + BASE_CHANNEL + channel, data, null );
	}

	@Override
	public void addConnectionListener( ConnectionListener listener )
	{
		connectionListeners.add( listener );
	}

	@Override
	public void removeConnectionListener( ConnectionListener listener )
	{
		connectionListeners.remove( listener );
	}

	@Override
	public void close()
	{
		try
		{
			client.stop();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void open()
	{
		try
		{
			client.start();
		}
		catch( Exception e )
		{
			// e.printStackTrace();
		}
	}
}
