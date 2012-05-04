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
package com.eviware.loadui.util.statistics.store;

import java.util.Map;
import java.util.Set;

import net.jcip.annotations.Immutable;

import com.eviware.loadui.api.statistics.store.Entry;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
 * Immutable implementation of the Entry, used to store data for a number of
 * statistics for a specific time.
 * 
 * @author dain.nilsson
 */

@Immutable
public class EntryImpl implements Entry
{
	private final long timestamp;
	private final ImmutableMap<String, Number> values;

	public EntryImpl( long timestamp, Map<String, Number> values )
	{
		this.timestamp = timestamp;
		this.values = ImmutableMap.copyOf( values );
	}

	@Override
	public long getTimestamp()
	{
		return timestamp;
	}

	@Override
	public Set<String> getNames()
	{
		return values.keySet();
	}

	@Override
	public Number getValue( String name )
	{
		return values.get( name );
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper( this ).add( "timestamp", timestamp ).add( "values", values ).toString();
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode( timestamp, values );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() == obj.getClass() )
		{
			EntryImpl other = ( EntryImpl )obj;
			if( Objects.equal( timestamp, other.timestamp ) && Objects.equal( values, other.values ) )
				return true;
		}

		return true;
	}
}
