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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.bayeux.server.ServerSession.RemoveListener;
import org.cometd.server.AbstractService;
import org.cometd.server.ext.AcknowledgedMessagesExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.ServerEndpoint;
import com.eviware.loadui.util.messaging.ChannelRoutingSupport;
import com.google.common.collect.Maps;

public class BayeuxServiceServerEndpoint extends AbstractService implements ServerEndpoint
{
	public final static Logger log = LoggerFactory.getLogger( BayeuxServiceServerEndpoint.class );

	private final static String CHANNEL = "/service" + MessageEndpoint.BASE_CHANNEL;

	private final Set<ConnectionListener> serverConnectionListeners = new HashSet<ConnectionListener>();
	private final Map<ServerSession, MessageEndpointImpl> sessions = new HashMap<ServerSession, MessageEndpointImpl>();
	private final ScheduledExecutorService timeoutWatcher = Executors.newSingleThreadScheduledExecutor();

	public BayeuxServiceServerEndpoint( BayeuxServer bayeuxServer )
	{
		super( bayeuxServer, "messageEndpoint" );
		bayeuxServer.addExtension( new AcknowledgedMessagesExtension() );

		addService( CHANNEL + "/**", "fireMessage" );
		addService( "/service/init", "initialize" );
	}

	public void fireMessage( ServerSession session, Message message )
	{
		if( session != null )
		{
			MessageEndpointImpl messageEndpoint = sessions.get( session );
			if( messageEndpoint == null )
				log.error( "Received message for unknown session:", message.getData() );
			else
				messageEndpoint.fireMessage( message );
		}
	}

	public void initialize( ServerSession session, String channel, Object data, String messageId )
	{
		if( session != null )
		{
			if( LoadUI.AGENT_VERSION.equals( data ) )
			{
				if( !sessions.containsKey( session ) )
					sessions.put( session, new MessageEndpointImpl( session ) );
			}
			else
			{
				log.warn( "Client attempted to connect with invalid version string. Mine: {}, Theirs: {}",
						LoadUI.AGENT_VERSION, data );
				send( session, channel, LoadUI.AGENT_VERSION, null );
			}
		}
	}

	@Override
	public void addConnectionListener( ConnectionListener listener )
	{
		serverConnectionListeners.add( listener );
	}

	@Override
	public void removeConnectionListener( ConnectionListener listener )
	{
		serverConnectionListeners.remove( listener );
	}

	@Override
	public Set<MessageEndpoint> getConnectedEndpoints()
	{
		return new HashSet<MessageEndpoint>( sessions.values() );
	}

	private class MessageEndpointImpl implements MessageEndpoint, RemoveListener
	{
		private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport();
		private final Set<ConnectionListener> connectionListeners = new HashSet<ConnectionListener>();
		private final HashMap<Long, Message> buffer = Maps.newHashMap();
		private final ServerSession session;
		private long nextSeq = Long.MIN_VALUE;
		private Future<?> timeoutFuture;

		public MessageEndpointImpl( ServerSession session )
		{
			this.session = session;

			session.addListener( this );

			for( ConnectionListener listener : new HashSet<ConnectionListener>( serverConnectionListeners ) )
				listener.handleConnectionChange( this, true );
		}

		public synchronized void fireMessage( Message message )
		{
			Long seq = ( Long )message.getExt().get( "seq" );
			if( seq.longValue() == nextSeq )
			{
				if( timeoutFuture != null )
				{
					timeoutFuture.cancel( true );
					timeoutFuture = null;
					// log.debug( "Canceling timeout, buffer size: {}",
					// buffer.size() );
				}

				doFire( message.getChannel(), message.getData() );
				nextSeq++ ;
				flushBuffer();
			}
			else
			{
				buffer.put( seq, message );
			}

			if( !buffer.isEmpty() && timeoutFuture == null )
			{
				// log.debug( "Scheduling timeout, buffer size: {}", buffer.size()
				// );
				timeoutFuture = timeoutWatcher.schedule( new Runnable()
				{
					@Override
					public void run()
					{
						synchronized( MessageEndpointImpl.this )
						{
							if( !buffer.isEmpty() )
							{
								log.error( "Message with SEQ: {} dropped!", nextSeq++ );
								ArrayList<Long> sequences = new ArrayList<Long>( buffer.keySet() );
								Collections.sort( sequences );
								boolean found = false;
								for( Long seq : sequences )
								{
									if( seq >= nextSeq )
									{
										nextSeq = seq;
										found = true;
										break;
									}
								}
								if( !found )
									nextSeq = sequences.get( 0 );

								flushBuffer();
							}
							timeoutFuture = buffer.isEmpty() ? null : timeoutWatcher.schedule( this, 5, TimeUnit.SECONDS );
						}
					}
				}, 5, TimeUnit.SECONDS );
			}
		}

		private void flushBuffer()
		{
			while( buffer.containsKey( nextSeq ) )
			{
				Message message = buffer.remove( nextSeq++ );
				doFire( message.getChannel(), message.getData() );
			}
		}

		private void doFire( String channel, Object data )
		{
			if( channel.startsWith( CHANNEL ) )
				routingSupport.fireMessage( channel.substring( CHANNEL.length() ), this, data );
		}

		@Override
		public void addConnectionListener( ConnectionListener listener )
		{
			connectionListeners.add( listener );
		}

		@Override
		public void addMessageListener( String channel, MessageListener listener )
		{
			routingSupport.addMessageListener( channel, listener );
		}

		@Override
		public synchronized void close()
		{
			session.disconnect();
			if( timeoutFuture != null )
			{
				timeoutFuture.cancel( true );
				timeoutFuture = null;
			}
			ArrayList<Long> sequences = new ArrayList<Long>( buffer.keySet() );
			Collections.sort( sequences );
			for( Long seq : sequences )
			{
				if( seq >= nextSeq )
				{
					Message message = buffer.remove( seq );
					doFire( message.getChannel(), message.getData() );
				}
			}
			for( Long seq : sequences )
			{
				if( seq < nextSeq )
				{
					Message message = buffer.remove( seq );
					doFire( message.getChannel(), message.getData() );
				}
				else
				{
					break;
				}
			}
			buffer.clear();
		}

		@Override
		public void open()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeConnectionListener( ConnectionListener listener )
		{
			connectionListeners.remove( listener );
		}

		@Override
		public void removeMessageListener( MessageListener listener )
		{
			routingSupport.removeMessageListener( listener );
		}

		@Override
		public void sendMessage( String channel, Object data )
		{
			send( session, channel, data, null );
		}

		@Override
		public void removed( ServerSession session, boolean timeout )
		{
			Set<ConnectionListener> listeners = new HashSet<ConnectionListener>( serverConnectionListeners );
			listeners.addAll( connectionListeners );
			for( ConnectionListener listener : listeners )
				listener.handleConnectionChange( this, false );

			connectionListeners.clear();

			sessions.remove( session );
		}

		@Override
		public String toString()
		{
			return MessageEndpointImpl.class.getSimpleName() + "[connected=" + session.isConnected() + ",handshook="
					+ session.isHandshook() + "]";
		}
	}
}
