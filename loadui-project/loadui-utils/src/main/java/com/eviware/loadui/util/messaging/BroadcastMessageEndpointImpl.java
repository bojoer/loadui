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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;

public class BroadcastMessageEndpointImpl implements BroadcastMessageEndpoint
{
	protected static final Logger log = LoggerFactory.getLogger( BroadcastMessageEndpointImpl.class );

	private final Set<MessageEndpoint> endpoints = new HashSet<MessageEndpoint>();
	private final ChannelRoutingSupport routingSupport = new ChannelRoutingSupport();
	private final Listener myListener = new Listener();

	@Override
	public Collection<MessageEndpoint> getEndpoints()
	{
		return Collections.unmodifiableSet( endpoints );
	}

	@Override
	public void deregisterEndpoint( MessageEndpoint endpoint )
	{
		if( endpoints.remove( endpoint ) )
			endpoint.removeMessageListener( myListener );
	}

	@Override
	public void registerEndpoint( MessageEndpoint endpoint )
	{
		if( endpoints.add( endpoint ) )
			endpoint.addMessageListener( "/**", myListener );
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
		for( MessageEndpoint endpoint : endpoints )
		{
			endpoint.sendMessage( channel, data );
		}
	}

	private class Listener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			routingSupport.fireMessage( channel, endpoint, data );
		}
	}

	// Not implemented, always "connected".
	@Override
	public void addConnectionListener( ConnectionListener listener )
	{
	}

	@Override
	public void close()
	{
	}

	@Override
	public void open()
	{
	}

	@Override
	public void removeConnectionListener( ConnectionListener listener )
	{
	}
}
