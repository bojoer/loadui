/*
 * Copyright 2011 SmartBear Software
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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.ext.AckExtension;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.VersionMismatchException;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.messaging.ChannelRoutingSupport;

public class BayeuxMessageEndpoint extends BayeuxClient implements MessageEndpoint
{
	public final static Logger log = LoggerFactory.getLogger( BayeuxMessageEndpoint.class );

	private final ScheduledExecutorService scheduledExecutor;
	private final ExecutorService executorService;
	private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport( this );
	private final Set<ConnectionListener> connectionListeners = new HashSet<ConnectionListener>();
	private final Runnable stateChecker = new StateChecker();
	private ScheduledFuture<?> stateCheckerFuture;
	private boolean open = false;
	private boolean connected = false;
	private long sequence = Long.MIN_VALUE;

	public BayeuxMessageEndpoint( String url, HttpClient httpClient )
	{
		super( url, new LongPollingTransport( new HashMap<String, Object>(), httpClient ) );

		addExtension( new AckExtension() );

		scheduledExecutor = BeanInjector.getBean( ScheduledExecutorService.class );
		executorService = BeanInjector.getBean( ExecutorService.class );

		try
		{
			// Replace the Logger in BayeuxClient with one that is static, so that
			// we can silence it easier.
			Field loggerField = BayeuxClient.class.getDeclaredField( "logger" );
			loggerField.setAccessible( true );
			loggerField.set( this, LoggerFactory.getLogger( BayeuxClient.class.getName() ) );
		}
		catch( SecurityException e )
		{
			e.printStackTrace();
		}
		catch( NoSuchFieldException e )
		{
			e.printStackTrace();
		}
		catch( IllegalArgumentException e )
		{
			e.printStackTrace();
		}
		catch( IllegalAccessException e )
		{
			e.printStackTrace();
		}
	}

	private void setConnected( boolean connected )
	{
		if( this.connected != connected )
		{
			this.connected = connected;
			if( open || !connected )
				for( ConnectionListener listener : connectionListeners )
					listener.handleConnectionChange( BayeuxMessageEndpoint.this, connected );

			if( connected )
			{
				newChannel( newChannelId( BASE_CHANNEL + "/**" ) ).subscribe( new ClientSessionChannel.MessageListener()
				{
					@Override
					public void onMessage( ClientSessionChannel arg0, Message message )
					{
						String channel = message.getChannel();
						if( channel.startsWith( BASE_CHANNEL ) && message.getData() != null )
							routingSupport.fireMessage( channel.substring( BASE_CHANNEL.length() ), message.getData() );
					}
				} );
				stateCheckerFuture = scheduledExecutor.scheduleAtFixedRate( stateChecker, 1, 1, TimeUnit.SECONDS );
				Message.Mutable message = newMessage();
				message.setChannel( "/service/init" );
				message.setData( LoadUI.AGENT_VERSION );
				enqueueSend( message );
			}
			else
			{
				stateCheckerFuture.cancel( true );
			}
		}
	}

	@Override
	protected void processConnect( Message.Mutable connect )
	{
		super.processConnect( connect );
		if( open )
			setConnected( connect.isSuccessful() );
	}

	@Override
	protected void processDisconnect( Message.Mutable disconnect )
	{
		super.processDisconnect( disconnect );
		setConnected( false );
	}

	@Override
	public void onMessages( List<Message.Mutable> messages )
	{
		// log.debug( "BayeuxMessageEndpoint.onMessages: {}", messages );
		for( Message.Mutable message : messages )
		{
			String channel = message.getChannel();
			if( message.getData() != null && channel.equals( "/service/init" ) )
			{
				if( !LoadUI.AGENT_VERSION.equals( message.getData() ) )
				{
					log.warn( "Cannot connect to server with different version number than the client: {} != {}",
							LoadUI.AGENT_VERSION, message.getData() );
					routingSupport.fireMessage( ERROR_CHANNEL, new VersionMismatchException( message.getData() == null ? "0"
							: message.getData().toString() ) );
				}
			}
			routingSupport.fireMessage( message.getChannel(), message.getData() );
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
	public synchronized void sendMessage( String channel, Object data )
	{
		Message.Mutable message = newMessage();
		message.setChannel( "/service" + BASE_CHANNEL + channel );
		message.setData( data );
		message.getExt( true ).put( "seq", sequence++ );
		enqueueSend( message );
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
	public synchronized void close()
	{
		if( open )
		{
			try
			{
				open = false;
				disconnect();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void open()
	{
		if( !open )
		{
			try
			{
				executorService.submit( new Runnable()
				{
					@Override
					public void run()
					{
						sequence = Long.MIN_VALUE;
						handshake();
						open = true;
						setConnected( isConnected() );
					}
				} );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
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