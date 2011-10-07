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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;

public class BroadcastMessageEndpointImpl implements BroadcastMessageEndpoint
{
	private final Set<MessageEndpoint> endpoints = new HashSet<MessageEndpoint>();
	private final Map<Pattern, MessageListener> listeners = new HashMap<Pattern, MessageListener>();
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
		if( !listeners.containsValue( listener ) )
		{
			if( channel.endsWith( "/*" ) )
				channel = Pattern.quote( channel.substring( 0, channel.length() - 1 ) ) + ".*";
			else if( channel.endsWith( "/**" ) )
				channel = Pattern.quote( channel.substring( 0, channel.length() - 1 ) ) + "[^/]*";
			else
				channel = Pattern.quote( channel );

			synchronized( listeners )
			{
				listeners.put( Pattern.compile( channel ), listener );
			}
		}
	}

	@Override
	public void removeMessageListener( MessageListener listener )
	{
		Iterator<Entry<Pattern, MessageListener>> it = listeners.entrySet().iterator();
		while( it.hasNext() )
		{
			Entry<Pattern, MessageListener> entry = it.next();

			if( entry.getValue().equals( listener ) )
			{
				it.remove();
				break;
			}
		}
	}

	@Override
	public void sendMessage( String channel, Object data )
	{
		for( MessageEndpoint endpoint : endpoints )
			endpoint.sendMessage( channel, data );
	}

	private class Listener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			Set<Map.Entry<Pattern, MessageListener>> entrySet = new HashSet<Map.Entry<Pattern, MessageListener>>(
					listeners.entrySet() );
			for( Map.Entry<Pattern, MessageListener> entry : entrySet )
				if( entry.getKey().matcher( channel ).matches() )
					entry.getValue().handleMessage( channel, endpoint, data );
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
