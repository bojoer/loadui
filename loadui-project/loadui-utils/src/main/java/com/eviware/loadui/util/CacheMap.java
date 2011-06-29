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
package com.eviware.loadui.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Cache class which caches values under keys using weak references for the
 * values. If no references to the value remain, the entry is discarded.
 * 
 * @author dain.nilsson
 * 
 * @param <K>
 * @param <V>
 */
public class CacheMap<K, V> implements Map<K, V>
{
	private final Map<K, WeakReference<V>> cache = new HashMap<K, WeakReference<V>>();

	private void compact()
	{
		for( K key : cache.keySet() )
			if( cache.get( key ).get() == null )
				cache.remove( key );
	}

	@Override
	public void clear()
	{
		cache.clear();
	}

	@Override
	public boolean containsKey( Object key )
	{
		return cache.containsKey( key ) && cache.get( key ).get() != null;
	}

	@Override
	public boolean containsValue( Object value )
	{
		for( WeakReference<V> ref : cache.values() )
			if( value.equals( ref.get() ) )
				return true;
		return false;
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		Set<Entry<K, V>> entries = new HashSet<Entry<K, V>>();
		compact();
		for( K key : cache.keySet() )
			entries.add( new CacheEntry( key ) );
		return entries;
	}

	@Override
	public V get( Object key )
	{
		return cache.containsKey( key ) ? cache.get( key ).get() : null;
	}

	@Override
	public boolean isEmpty()
	{
		compact();
		return cache.isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		compact();
		return cache.keySet();
	}

	@Override
	public V put( K key, V value )
	{
		V oldVal = get( key );
		cache.put( key, new WeakReference<V>( value ) );
		return oldVal;
	}

	@Override
	public void putAll( Map<? extends K, ? extends V> m )
	{
		for( Entry<? extends K, ? extends V> entry : m.entrySet() )
			put( entry.getKey(), entry.getValue() );
	}

	@Override
	public V remove( Object key )
	{
		WeakReference<V> removed = cache.remove( key );
		return removed == null ? null : removed.get();
	}

	@Override
	public int size()
	{
		compact();
		return cache.size();
	}

	@Override
	public Collection<V> values()
	{
		Collection<V> values = new ArrayList<V>();
		for( WeakReference<V> valRef : cache.values() )
			if( valRef.get() != null )
				values.add( valRef.get() );
		return values;
	}

	public V getOrCreate( K key, Callable<V> create )
	{
		if( containsKey( key ) )
			return get( key );
		try
		{
			V value = create.call();
			put( key, value );
			return value;
		}
		catch( Exception e )
		{
			return null;
		}
	}

	private class CacheEntry implements Entry<K, V>
	{
		private final K key;

		public CacheEntry( K key )
		{
			this.key = key;
		}

		@Override
		public K getKey()
		{
			return key;
		}

		@Override
		public V getValue()
		{
			return cache.get( key ).get();
		}

		@Override
		public V setValue( V value )
		{
			return put( key, value );
		}
	}
}
