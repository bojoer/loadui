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
package com.eviware.loadui.util.messaging;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.traits.Releasable;

public class MessageEndpointSupport implements Releasable
{
	private final MessageEndpoint target;
	private final MessageEndpoint source;
	private final Map<MessageListener, MessageListenerProxy> messageProxies = new WeakHashMap<MessageListener, MessageListenerProxy>();
	private final Map<ConnectionListener, ConnectionListenerProxy> connectionProxies = new WeakHashMap<ConnectionListener, ConnectionListenerProxy>();
	private final Set<MessageListener> errorListeners = new HashSet<MessageListener>();

	public MessageEndpointSupport( MessageEndpoint wrapper, MessageEndpoint source )
	{
		this.target = wrapper;
		this.source = source;
	}

	public MessageEndpoint getSource()
	{
		return source;
	}

	public void fireError( Throwable error )
	{
		for( MessageListener listener : new ArrayList<MessageListener>( errorListeners ) )
			listener.handleMessage( MessageEndpoint.ERROR_CHANNEL, target, error );
	}

	public void addMessageListener( String channel, MessageListener listener )
	{
		if( !messageProxies.containsKey( listener ) )
			messageProxies.put( listener, new MessageListenerProxy( listener ) );
		source.addMessageListener( channel, messageProxies.get( listener ) );
		if( MessageEndpoint.ERROR_CHANNEL.equals( channel ) )
			errorListeners.add( listener );
	}

	public void removeMessageListener( MessageListener listener )
	{
		source.removeMessageListener( messageProxies.remove( listener ) );
		errorListeners.remove( listener );
	}

	public void addConnectionListener( ConnectionListener listener )
	{
		if( !connectionProxies.containsKey( listener ) )
			connectionProxies.put( listener, new ConnectionListenerProxy( listener ) );
		source.addConnectionListener( connectionProxies.get( listener ) );
	}

	public void removeConnectionListener( ConnectionListener listener )
	{
		source.removeConnectionListener( connectionProxies.remove( listener ) );
	}

	public void sendMessage( String channel, Object data )
	{
		source.sendMessage( channel, data );
	}

	public void open()
	{
		source.open();
	}

	public void close()
	{
		source.close();
	}

	@Override
	public void release()
	{
		for( ConnectionListenerProxy listener : connectionProxies.values() )
			source.removeConnectionListener( listener );
		connectionProxies.clear();
		for( MessageListenerProxy listener : messageProxies.values() )
			source.removeMessageListener( listener );
		messageProxies.clear();
		errorListeners.clear();
	}

	private class MessageListenerProxy implements MessageListener
	{
		private final WeakReference<MessageListener> listenerRef;

		public MessageListenerProxy( MessageListener listener )
		{
			listenerRef = new WeakReference<MessageListener>( listener );
		}

		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			final MessageListener listener = listenerRef.get();
			if( listener != null )
			{
				listener.handleMessage( channel, target, data );
			}
		}
	}

	private class ConnectionListenerProxy implements ConnectionListener
	{
		private final WeakReference<ConnectionListener> listenerRef;

		public ConnectionListenerProxy( ConnectionListener listener )
		{
			listenerRef = new WeakReference<ConnectionListener>( listener );
		}

		@Override
		public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
		{
			final ConnectionListener listener = listenerRef.get();
			if( listener != null )
			{
				listener.handleConnectionChange( target, connected );
			}
		}
	}
}
