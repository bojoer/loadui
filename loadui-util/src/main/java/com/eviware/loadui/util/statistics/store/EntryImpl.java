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
	private final long timestamp;
	private final Map<String, Number> values;

	public EntryImpl( long timestamp, Map<String, Number> values )
	{
		this( timestamp, values, false );
	}

	public EntryImpl( long timestamp, Map<String, Number> values, boolean dontCloneMap )
	{
		this.timestamp = timestamp;
		this.values = Collections.unmodifiableMap( dontCloneMap ? values : new HashMap<String, Number>( values ) );
	}

	@Override
	public long getTimestamp()
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

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder( "Entry<" );
		sb.append( timestamp ).append( ">[" );
		sb.append( values.toString() ).append( "]" );
		return sb.toString();
	}
}
