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

}
