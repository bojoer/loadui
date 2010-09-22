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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.messaging.ChannelRoutingSupport;

public class BayeuxMessageEndpoint extends BayeuxClient implements MessageEndpoint
{
	public final static Logger log = LoggerFactory.getLogger( BayeuxMessageEndpoint.class );

	private final ScheduledExecutorService scheduledExecutor;
	private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport( this );
	private final Set<ConnectionListener> connectionListeners = new HashSet<ConnectionListener>();
	private final Runnable stateChecker = new StateChecker();
	private ScheduledFuture<?> stateCheckerFuture;
	private boolean connected = false;

	public BayeuxMessageEndpoint( String url, HttpClient httpClient )
	{
		super( url, new LongPollingTransport( new HashMap<String, Object>(), httpClient ) );

		scheduledExecutor = BeanInjector.getBean( ScheduledExecutorService.class );
	}

	private void setConnected( boolean connected )
	{
		if( this.connected != connected )
		{
			this.connected = connected;
			for( ConnectionListener listener : connectionListeners )
				listener.handleConnectionChange( BayeuxMessageEndpoint.this, connected );

			if( connected )
			{
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
				stateCheckerFuture = scheduledExecutor.scheduleAtFixedRate( stateChecker, 1, 1, TimeUnit.SECONDS );
			}
			else
			{
				stateCheckerFuture.cancel( true );
			}
		}
	}

	@Override
	protected void processConnect( Message connect )
	{
		super.processConnect( connect );
		setConnected( connect.isSuccessful() );
	}

	@Override
	protected void processDisconnect( Message disconnect )
	{
		super.processDisconnect( disconnect );
		setConnected( false );
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
		log.debug( "Sending message: {} on channel: {}", data, message );
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
			setConnected( isConnected() );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	private class StateChecker implements Runnable
	{
		@Override
		public void run()
		{
			if( !isConnected() )
			{
				log.debug( "Connection failure detected!" );
				setConnected( false );
			}
		}
	}
}