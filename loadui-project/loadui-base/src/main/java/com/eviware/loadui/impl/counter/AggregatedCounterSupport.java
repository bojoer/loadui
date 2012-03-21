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

import java.util.Set;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.CounterEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

public class AggregatedCounterSupport extends CounterSupport
{
	private final LoadingCache<String, Long> valueCache = CacheBuilder.newBuilder().build(
			new CacheLoader<String, Long>()
			{
				@Override
				public Long load( String key ) throws Exception
				{
					long valueSum = AggregatedCounterSupport.super.getCounterValue( key );
					synchronized( AggregatedCounterSupport.this )
					{
						for( CounterHolder child : children )
							valueSum += child.getCounter( key ).get();
					}

					return valueSum;
				}
			} );
	private final Set<CounterHolder> children = Sets.newHashSet();
	private final CounterListener listener = new CounterListener();

	@Override
	protected long getCounterValue( String name )
	{
		return valueCache.getUnchecked( name );
	}

	@Override
	protected long incrementCounterValue( String name, long value )
	{
		super.incrementCounterValue( name, value );
		valueCache.invalidate( name );

		return getCounterValue( name );
	}

	@Override
	public void resetCounters()
	{
		super.resetCounters();
		valueCache.invalidateAll();
	}

	public synchronized void addChild( CounterHolder holder )
	{
		if( children.add( holder ) )
		{
			holder.addEventListener( CounterEvent.class, listener );
			valueCache.invalidateAll();
		}
	}

	public synchronized void removeChild( CounterHolder holder )
	{
		if( children.remove( holder ) )
		{
			holder.removeEventListener( CounterEvent.class, listener );
			valueCache.invalidateAll();
		}
	}

	private class CounterListener implements EventHandler<CounterEvent>
	{
		@Override
		public void handleEvent( CounterEvent event )
		{
			String name = event.getKey();
			valueCache.invalidate( name );
			owner.fireEvent( new CounterEvent( ( CounterHolder )owner, name, event.getValue() ) );
		}
	}
}
