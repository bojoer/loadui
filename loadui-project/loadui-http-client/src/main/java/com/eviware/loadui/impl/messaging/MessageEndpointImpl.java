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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.util.messaging.ChannelRoutingSupport;



public class MessageEndpointImpl extends BayeuxClient implements MessageEndpoint
{
	private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport( this );
	private final Set<ConnectionListener> connectionListeners = new HashSet<ConnectionListener>();
	private boolean connected = false;

	private Logger log = LoggerFactory.getLogger(MessageEndpointImpl.class);
	
	public MessageEndpointImpl( String url, HttpClient httpClient )
	{
		super( url, new LongPollingTransport( new HashMap<String, Object>(), httpClient ) );
	}

	@Override
	protected void processConnect( Message connect )
	{
		super.processConnect( connect );
		if( connected != connect.isSuccessful() )
		{
			connected = !connected;
			for( ConnectionListener listener : connectionListeners )
				listener.handleConnectionChange( MessageEndpointImpl.this, connected );
			if( connected )
				newChannel( newChannelId( BASE_CHANNEL + "/**" ) ).subscribe( new ClientSessionChannel.MessageListener()
				{
					@Override
					public void onMessage( ClientSessionChannel arg0, Message message )
					{
						System.out.println( "MessageListener: " + arg0 + ", " + message );
						String channel = message.getChannel();
						if( channel.startsWith( BASE_CHANNEL ) && message.getData() != null )
							routingSupport.fireMessage( channel.substring( BASE_CHANNEL.length() ), message.getData() );
					}
				} );
		}
	}

	@Override
	protected void processDisconnect(Message disconnect)
	{
		// TODO Auto-generated method stub
		super.processDisconnect(disconnect);
		log.debug("Disconected processed");
		log.debug(disconnect.getChannel() + " --- " + disconnect.getData());
		connected = false;
	}
	
	@Override
	protected void processHandshake(Message arg0)
	{
		// TODO Auto-generated method stub
		super.processHandshake(arg0);
		log.debug("Handshake processed");
	}
	
	@Override
	public void onMessages( List<Message.Mutable> messages )
	{
		for( Message.Mutable message : messages )
			routingSupport.fireMessage( message.getChannel(), message.getData() );
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
		Message.Mutable message = newMessage();
		message.setChannel( "/service" + BASE_CHANNEL + channel );
		message.setData( data );
		send( message );
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
			disconnect();
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
			handshake();
			connected = isConnected();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}