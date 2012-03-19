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
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class ChannelRoutingSupport
{
	protected static final Logger log = LoggerFactory.getLogger( ChannelRoutingSupport.class );

	private final Multimap<Pattern, MessageListener> listeners = Multimaps.synchronizedSetMultimap( HashMultimap
			.<Pattern, MessageListener> create() );

	public void addMessageListener( String channel, MessageListener listener )
	{
		log.debug( "Adding listener: {} for channel: {}", listener, channel );
		if( !listeners.containsValue( listener ) )
		{
			if( channel.endsWith( "/**" ) )
				channel = Pattern.quote( channel.substring( 0, channel.length() - 2 ) ) + ".*";
			else if( channel.endsWith( "/*" ) )
				channel = Pattern.quote( channel.substring( 0, channel.length() - 1 ) ) + "[^/]*";
			else
				channel = Pattern.quote( channel );

			//log.debug( "Pattern is: {}", Pattern.compile( channel ) );
			listeners.put( Pattern.compile( channel ), listener );
		}
	}

	public void removeMessageListener( MessageListener listener )
	{
		for( Pattern pattern : ImmutableSet.copyOf( listeners.keySet() ) )
		{
			listeners.remove( pattern, listener );
		}
	}

	public void fireMessage( String channel, MessageEndpoint endpoint, Object data )
	{
		//log.debug( "Handling broadcast to: {}", channel );
		for( Map.Entry<Pattern, Collection<MessageListener>> entry : ImmutableSet.copyOf( listeners.asMap().entrySet() ) )
		{
			if( entry.getKey().matcher( channel ).matches() )
			{
				//log.debug( "MATCH: {}", entry.getKey() );
				for( MessageListener listener : ImmutableSet.copyOf( entry.getValue() ) )
				{
					//log.debug( "Handler: {} handling...", listener );
					listener.handleMessage( channel, endpoint, data );
				}
			}
		}
	}
}
