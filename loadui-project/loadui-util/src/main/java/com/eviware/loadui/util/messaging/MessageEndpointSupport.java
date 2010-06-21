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
package com.eviware.loadui.util.messaging;

import java.util.Map;
import java.util.WeakHashMap;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;

public class MessageEndpointSupport
{
	private final MessageEndpoint target;
	private final MessageEndpoint source;
	private final Map<MessageListener, MessageListenerProxy> messageProxies = new WeakHashMap<MessageListener, MessageListenerProxy>();
	private final Map<ConnectionListener, ConnectionListenerProxy> connectionProxies = new WeakHashMap<ConnectionListener, ConnectionListenerProxy>();

	public MessageEndpointSupport( MessageEndpoint wrapper, MessageEndpoint source )
	{
		this.target = wrapper;
		this.source = source;
	}

	public MessageEndpoint getSource()
	{
		return source;
	}

	public void addMessageListener( String channel, MessageListener listener )
	{
		if( !messageProxies.containsKey( listener ) )
			messageProxies.put( listener, new MessageListenerProxy( listener ) );
		source.addMessageListener( channel, messageProxies.get( listener ) );
	}

	public void removeMessageListener( MessageListener listener )
	{
		source.removeMessageListener( messageProxies.get( listener ) );
	}

	public void addConnectionListener( ConnectionListener listener )
	{
		if( !connectionProxies.containsKey( listener ) )
			connectionProxies.put( listener, new ConnectionListenerProxy( listener ) );
		source.addConnectionListener( connectionProxies.get( listener ) );
	}

	public void removeConnectionListener( ConnectionListener listener )
	{
		source.removeConnectionListener( connectionProxies.get( listener ) );
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

	private class MessageListenerProxy implements MessageListener
	{
		private final MessageListener listener;

		public MessageListenerProxy( MessageListener listener )
		{
			this.listener = listener;
		}

		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			listener.handleMessage( channel, target, data );
		}
	}

	private class ConnectionListenerProxy implements ConnectionListener
	{
		private final ConnectionListener listener;

		public ConnectionListenerProxy( ConnectionListener listener )
		{
			this.listener = listener;
		}

		@Override
		public void handleConnectionChange( MessageEndpoint endpoint, boolean connected )
		{
			listener.handleConnectionChange( target, connected );
		}
	}
}
