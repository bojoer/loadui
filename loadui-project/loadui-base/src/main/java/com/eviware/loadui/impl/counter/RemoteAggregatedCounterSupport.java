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
package com.eviware.loadui.impl.counter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.counter.CounterSynchronizer;
import com.eviware.loadui.api.counter.CounterSynchronizer.Aggregator;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CounterEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.model.ModelItem;

public class RemoteAggregatedCounterSupport extends AggregatedCounterSupport implements Aggregator
{
	private final Map<MessageEndpoint, Map<String, Long>> remoteValues = new HashMap<MessageEndpoint, Map<String, Long>>();
	private final Map<String, Long> summedValues = new HashMap<String, Long>();
	private final CounterSynchronizer counterSynchronizer;

	public RemoteAggregatedCounterSupport( CounterSynchronizer counterSynchronizer )
	{
		this.counterSynchronizer = counterSynchronizer;
	}

	@Override
	public void init( ModelItem modelItem )
	{
		super.init( modelItem );

		counterSynchronizer.syncAggregator( modelItem.getId(), this );

		modelItem.addEventListener( BaseEvent.class, new EventHandler<BaseEvent>()
		{
			@Override
			public void handleEvent( BaseEvent event )
			{
				if( ModelItem.RELEASED.equals( event.getKey() ) )
					counterSynchronizer.unsyncAggregator( owner.getId() );
			}
		} );
	}

	@Override
	protected long getCounterValue( String name )
	{
		synchronized( summedValues )
		{
			return super.getCounterValue( name ) + ( summedValues.containsKey( name ) ? summedValues.get( name ) : 0 );
		}
	}

	@Override
	public void resetCounters()
	{
		super.resetCounters();
		synchronized( summedValues )
		{
			summedValues.clear();
			remoteValues.clear();
		}
	}

	@Override
	public void updateChildValues( MessageEndpoint child, Map<String, String> values )
	{
		synchronized( summedValues )
		{
			if( !remoteValues.containsKey( child ) )
				remoteValues.put( child, new HashMap<String, Long>() );

			Map<String, Long> valuesMap = remoteValues.get( child );
			for( Entry<String, String> entry : values.entrySet() )
			{
				String name = entry.getKey();
				long value = Long.parseLong( entry.getValue() );
				Long oldValue = valuesMap.put( name, value );
				long delta = ( oldValue == null ) ? value : ( value - oldValue );
				if( delta > 0 )
				{
					Long prevVal = summedValues.get( name );
					long newVal = prevVal == null ? delta : ( prevVal + delta );
					summedValues.put( name, newVal );
					owner.fireEvent( new CounterEvent( ( CounterHolder )owner, name, delta ) );
				}
			}
		}
	}
}
