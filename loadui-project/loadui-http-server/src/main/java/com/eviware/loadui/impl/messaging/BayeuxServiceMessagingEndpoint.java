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

import java.util.HashSet;
import java.util.Set;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.util.messaging.ChannelRoutingSupport;

public class BayeuxServiceMessagingEndpoint implements MessageEndpoint
{
	private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport();
	private final BayeuxServiceServerEndpoint provider;
	private final Set<ConnectionListener> connectionListeners = new HashSet<ConnectionListener>();
	private boolean open = true;

	public BayeuxServiceMessagingEndpoint( BayeuxServiceServerEndpoint provider )
	{
		this.provider = provider;
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
		// provider.sendMessage( BASE_CHANNEL + channel, data );
	}

	public void fireMessage( String channel, Object data )
	{
		if( open )
		{
			routingSupport.fireMessage( channel, this, data );
		}
	}

	@Override
	public void addConnectionListener( ConnectionListener listener )
	{
		connectionListeners.add( listener );
	}

	@Override
	public void close()
	{
		if( open )
		{
			open = false;
			for( ConnectionListener listener : connectionListeners )
				listener.handleConnectionChange( this, false );
		}
	}

	@Override
	public void open()
	{
		if( !open )
		{
			open = true;
			for( ConnectionListener listener : connectionListeners )
				listener.handleConnectionChange( this, true );
		}
	}

	@Override
	public void removeConnectionListener( ConnectionListener listener )
	{
		connectionListeners.remove( listener );
	}
}
