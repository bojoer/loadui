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
package com.eviware.loadui.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Maps.
 * 
 * @author dain.nilsson
 */
public class MapUtils
{
	/**
	 * DEPRECATED! Use Google Guava instead! Starts building a new Map.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param keyType
	 *           The type to use for keys.
	 * @param valueType
	 *           The type to use for values.
	 * @return A new instance of MapBuilder, containing an empty Map<T1, T2>.
	 */
	@Deprecated
	public static <K, V> MapBuilder<K, V> build( Class<K> keyType, Class<V> valueType )
	{
		return new MapBuilder<K, V>();
	}

	/**
	 * Gets a value from the map, or if the value does not exist, a default
	 * value.
	 * 
	 * @param map
	 *           The map to get a value from.
	 * @param key
	 *           The key whose associated value is to be returned.
	 * @param defaultValue
	 *           The default value to return if the map does not contain a
	 *           mapping for the given key.
	 * @return The value to which this map maps the specified key, or the default
	 *         value if the map contains no mapping for this key.
	 */
	@SuppressWarnings( "unchecked" )
	public static <V> V getOr( Map<?, ?> map, Object key, V defaultValue )
	{
		if( map.containsKey( key ) )
		{
			Object value = map.get( key );
			if( defaultValue.getClass().isInstance( value ) )
				return ( V )value;
		}

		return defaultValue;
	}

	@Deprecated
	public static class MapBuilder<K, V>
	{

		private final Map<K, V> map;

		private MapBuilder()
		{
			map = new HashMap<K, V>();
		}

		/**
		 * Adds a key-value pair to the Map.
		 * 
		 * @param key
		 * @param value
		 * @return The MapBuilder.
		 */
		public MapBuilder<K, V> put( K key, V value )
		{
			map.put( key, value );

			return this;
		}

		/**
		 * Returns the built Map.
		 * 
		 * @return The built Map.
		 */
		public Map<K, V> get()
		{
			return map;
		}

		/**
		 * Returns an immutable view of the Map.
		 * 
		 * @return The build Map, immutable.
		 */
		public Map<K, V> getImmutable()
		{
			return Collections.unmodifiableMap( map );
		}
	}
}
