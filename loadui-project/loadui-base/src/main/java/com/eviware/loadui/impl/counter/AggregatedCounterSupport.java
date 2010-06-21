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
package com.eviware.loadui.impl.counter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.CounterEvent;
import com.eviware.loadui.api.events.EventHandler;

public class AggregatedCounterSupport extends CounterSupport
{
	private final Map<String, Long> cachedValues = new HashMap<String, Long>();
	private final Set<CounterHolder> children = new HashSet<CounterHolder>();
	private final CounterListener listener = new CounterListener();

	@Override
	protected long getCounterValue( String name )
	{
		synchronized( cachedValues )
		{
			cachedValues.remove( name );
			if( !cachedValues.containsKey( name ) )
			{
				long current = super.getCounterValue( name );
				for( CounterHolder child : children )
					current += child.getCounter( name ).get();
				cachedValues.put( name, current );
			}
			return cachedValues.get( name );
		}
	}

	@Override
	protected long incrementCounterValue( String name )
	{
		super.incrementCounterValue( name );
		synchronized( cachedValues )
		{
			if( cachedValues.containsKey( name ) )
				cachedValues.put( name, cachedValues.get( name ) + 1 );
		}

		return getCounterValue( name );
	}

	@Override
	public void resetCounters()
	{
		super.resetCounters();
		synchronized( cachedValues )
		{
			cachedValues.clear();
		}
	}

	public void addChild( CounterHolder holder )
	{
		synchronized( cachedValues )
		{
			if( children.add( holder ) )
			{
				holder.addEventListener( CounterEvent.class, listener );
				cachedValues.clear();
			}
		}
	}

	public void removeChild( CounterHolder holder )
	{
		synchronized( cachedValues )
		{
			if( children.remove( holder ) )
			{
				holder.removeEventListener( CounterEvent.class, listener );
				cachedValues.clear();
			}
		}
	}

	private class CounterListener implements EventHandler<CounterEvent>
	{
		@Override
		public void handleEvent( CounterEvent event )
		{
			String name = event.getKey();
			synchronized( cachedValues )
			{
				long incCount = event.getValue();
				if( cachedValues.containsKey( name ) )
					cachedValues.put( name, cachedValues.get( name ) + incCount );
				owner.fireEvent( new CounterEvent( ( CounterHolder )owner, name, incCount ) );
			}
		}
	}
}
