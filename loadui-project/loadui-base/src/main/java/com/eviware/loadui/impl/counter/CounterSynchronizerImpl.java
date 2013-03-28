/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.counter;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.counter.CounterSynchronizer;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.ModelItem;

public class CounterSynchronizerImpl implements CounterSynchronizer
{
	public final static Logger log = LoggerFactory.getLogger( CounterSynchronizerImpl.class );

	private final BaseEventListener listener = new BaseEventListener();
	private final AggregateListener aggregateListener = new AggregateListener();
	private final Map<CounterHolder, MessageEndpoint> holders = new HashMap<>();
	private final Map<String, Aggregator> aggregators = new HashMap<>();

	@Override
	public void syncCounters( CounterHolder counterHolder, MessageEndpoint endpoint )
	{
		synchronized( holders )
		{
			if( !holders.containsKey( counterHolder ) )
			{
				holders.put( counterHolder, endpoint );
				if( counterHolder instanceof ModelItem )
					counterHolder.addEventListener( BaseEvent.class, listener );
				endpoint.addMessageListener( CHANNEL, aggregateListener );
			}
		}
	}

	@Override
	public void unsyncCounters( CounterHolder counterHolder )
	{
		synchronized( holders )
		{
			if( holders.containsKey( counterHolder ) )
			{
				MessageEndpoint endpoint = holders.remove( counterHolder );
				if( !holders.containsValue( endpoint ) )
				{
					endpoint.removeMessageListener( aggregateListener );
				}
				if( counterHolder instanceof ModelItem )
				{
					counterHolder.removeEventListener( BaseEvent.class, listener );
				}
			}
		}
	}

	@Override
	public void syncAggregator( String ownerId, Aggregator aggregator )
	{
		aggregators.put( ownerId, aggregator );
	}

	@Override
	public void unsyncAggregator( String ownerId )
	{
		aggregators.remove( ownerId );
	}

	private class BaseEventListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( ModelItem.RELEASED.equals( event.getKey() ) )
			{
				unsyncCounters( ( CounterHolder )event.getSource() );
			}
		}
	}

	private class AggregateListener implements MessageListener
	{
		@Override
		@SuppressWarnings( "unchecked" )
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			Map<String, String> message = ( Map<String, String> )data;
			Aggregator aggregator = aggregators.get( message.remove( COUNTER_HOLDER_ID ) );
			if( aggregator != null )
			{
				aggregator.updateChildValues( endpoint, message );
			}
		}
	}
}
