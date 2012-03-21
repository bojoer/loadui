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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.CounterEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.impl.model.ModelItemImpl;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;

public class CounterSupport
{
	protected ModelItemImpl<?> owner;
	private final LoadingCache<String, AtomicLong> counters = CacheBuilder.newBuilder().build(
			new CacheLoader<String, AtomicLong>()
			{
				@Override
				public AtomicLong load( String key ) throws Exception
				{
					return new AtomicLong();
				}
			} );

	public void init( ModelItemImpl<?> owner )
	{
		if( !( owner instanceof CounterHolder ) )
			throw new IllegalArgumentException(
					"CounterSupport requires a ModelItemImpl which implements the CounterHolder interface, got " + owner );

		this.owner = owner;
		owner.addEventListener( ActionEvent.class, new ActionListener() );
	}

	public Counter getCounter( String name )
	{
		return new CounterImpl( name );
	}

	public Collection<String> getCounterNames()
	{
		return ImmutableSet.copyOf( counters.asMap().keySet() );
	}

	public void resetCounters()
	{
		counters.invalidateAll();
	}

	protected long getCounterValue( String name )
	{
		AtomicLong atomicLong = counters.getIfPresent( name );
		return atomicLong != null ? atomicLong.get() : 0;
	}

	protected long incrementCounterValue( String name, long value )
	{
		counters.getUnchecked( name ).addAndGet( value );

		long current = getCounterValue( name );
		owner.fireEvent( new CounterEvent( ( CounterHolder )owner, name, 1 ) );
		return current;
	}

	private class CounterImpl implements Counter
	{
		private final String name;

		public CounterImpl( String name )
		{
			this.name = name;
		}

		@Override
		public long get()
		{
			return getCounterValue( name );
		}

		@Override
		public void increment()
		{
			incrementCounterValue( name, 1 );
		}

		@Override
		public void increment( long value )
		{
			incrementCounterValue( name, value );
		}

		@Override
		public Class<Long> getType()
		{
			return Long.class;
		}

		@Override
		public Long getValue()
		{
			return get();
		}
	}

	private class ActionListener implements EventHandler<ActionEvent>
	{
		@Override
		public void handleEvent( ActionEvent event )
		{
			if( CounterHolder.COUNTER_RESET_ACTION.equals( event.getKey() ) )
				resetCounters();
		}
	}
}
