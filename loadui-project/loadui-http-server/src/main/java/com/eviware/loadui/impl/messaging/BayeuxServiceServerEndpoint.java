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
import java.util.Map;
import java.util.Set;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.bayeux.server.ServerSession.RemoveListener;
import org.cometd.server.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.ServerEndpoint;
import com.eviware.loadui.util.messaging.ChannelRoutingSupport;

public class BayeuxServiceServerEndpoint extends AbstractService implements ServerEndpoint
{
	public static Logger log = LoggerFactory.getLogger( BayeuxServiceServerEndpoint.class );

	private final static String CHANNEL = "/service" + MessageEndpoint.BASE_CHANNEL;
	private final Set<ConnectionListener> serverConnectionListeners = new HashSet<ConnectionListener>();
	private final Map<ServerSession, MessageEndpointImpl> sessions = new HashMap<ServerSession, MessageEndpointImpl>();

	public BayeuxServiceServerEndpoint( BayeuxServer bayeuxServer )
	{
		super( bayeuxServer, "messageEndpoint" );
		addService( CHANNEL + "/**", "fireMessage" );
	}

	public void fireMessage( ServerSession session, String channel, Object data, String messageId )
	{
		if( !sessions.containsKey( session ) )
			sessions.put( session, new MessageEndpointImpl( session ) );

		sessions.get( session ).fireMessage( channel, data );
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

	private class MessageEndpointImpl implements MessageEndpoint, RemoveListener
	{
		private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport( this );
		private final Set<ConnectionListener> connectionListeners = new HashSet<ConnectionListener>();
		private final ServerSession session;

		public MessageEndpointImpl( ServerSession session )
		{
			this.session = session;

			session.addListener( this );

			for( ConnectionListener listener : new HashSet<ConnectionListener>( serverConnectionListeners ) )
				listener.handleConnectionChange( this, true );
		}

		public void fireMessage( String channel, Object data )
		{
			if( channel.startsWith( CHANNEL ) )
				routingSupport.fireMessage( channel.substring( CHANNEL.length() ), data );
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
		public void close()
		{
			session.disconnect();
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
	}
}
