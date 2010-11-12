package com.eviware.loadui.util.statistics.store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.statistics.store.Entry;

/**
 * Immutable implementation of the Entry, used to store data for a number of
 * statistics for a specific time.
 * 
 * @author dain.nilsson
 */
public class EntryImpl implements Entry
{
	private final int timestamp;
	private final Map<String, Number> values;

	public EntryImpl( int timestamp, Map<String, Number> values )
	{
		this( timestamp, values, false );
	}

	public EntryImpl( int timestamp, Map<String, Number> values, boolean dontCloneMap )
	{
		this.timestamp = timestamp;
		this.values = Collections.unmodifiableMap( dontCloneMap ? values : new HashMap<String, Number>( values ) );
	}

	@Override
	public int getTimestamp()
	{
		return timestamp;
	}

	@Override
	public Collection<String> getNames()
	{
		return values.keySet();
	}

	@Override
	public Number getValue( String name )
	{
		return values.get( name );
	}
}
