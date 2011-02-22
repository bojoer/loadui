/*
 * Copyright 2011 eviware software ab
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;

public class ChannelRoutingSupport
{
	private final Map<Pattern, MessageListener> listeners = new HashMap<Pattern, MessageListener>();
	private final MessageEndpoint endpoint;

	public ChannelRoutingSupport( MessageEndpoint endpoint )
	{
		this.endpoint = endpoint;
	}

	public void addMessageListener( String channel, MessageListener listener )
	{
		if( !listeners.containsValue( listener ) )
		{
			if( channel.endsWith( "/*" ) )
				channel = Pattern.quote( channel.substring( 0, channel.length() - 1 ) ) + ".*";
			else if( channel.endsWith( "/**" ) )
				channel = Pattern.quote( channel.substring( 0, channel.length() - 2 ) ) + "[^/]*";
			else
				channel = Pattern.quote( channel );

			listeners.put( Pattern.compile( channel ), listener );
		}
	}

	public void removeMessageListener( MessageListener listener )
	{
		for( Entry<Pattern, MessageListener> entry : listeners.entrySet() )
		{
			if( entry.getValue() == listener )
			{
				listeners.remove( entry.getKey() );
				return;
			}
		}
	}

	public void fireMessage( String channel, Object data )
	{
		List<Map.Entry<Pattern, MessageListener>> entries = new ArrayList<Map.Entry<Pattern, MessageListener>>(
				listeners.entrySet() );
		for( Map.Entry<Pattern, MessageListener> entry : entries )
		{
			if( entry.getKey().matcher( channel ).matches() )
			{
				try
				{
					entry.getValue().handleMessage( channel, endpoint, data );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
	}
}
