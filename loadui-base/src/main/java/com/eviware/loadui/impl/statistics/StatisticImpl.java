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
package com.eviware.loadui.impl.statistics;

import java.util.Iterator;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;

public class StatisticImpl<T extends Number> implements Statistic<T>
{
	private final ExecutionManager manager;
	private final Class<T> type;
	private final String trackId;
	private final StatisticVariable variable;
	private final String name;
	private final String source;

	public StatisticImpl( ExecutionManager manager, String trackId, StatisticVariable variable, String name,
			String source, Class<T> type )
	{
		this.manager = manager;
		this.type = type;
		this.trackId = trackId;
		this.variable = variable;
		this.name = name;
		this.source = source;
	}

	@Override
	public Class<T> getType()
	{
		return type;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public T getValue()
	{
		return ( T )manager.getTrack( trackId ).getLastEntry( source ).getValue( name );
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public StatisticVariable getStatisticVariable()
	{
		return variable;
	}

	@Override
	public Iterable<DataPoint<T>> getPeriod( int start, int end )
	{
		return new DataPointIterable( manager.getTrack( trackId ).getRange( source, start, end ), name );
	}

	private class DataPointIterable implements Iterable<DataPoint<T>>
	{
		private final Iterable<Entry> iterable;
		private final String key;

		public DataPointIterable( Iterable<Entry> iterable, String key )
		{
			this.iterable = iterable;
			this.key = key;
		}

		@Override
		public Iterator<DataPoint<T>> iterator()
		{
			return new DataPointIterator( iterable.iterator(), key );
		}
	}

	private class DataPointIterator implements Iterator<DataPoint<T>>
	{
		private final Iterator<Entry> iterator;
		private final String key;

		public DataPointIterator( Iterator<Entry> iterator, String key )
		{
			this.iterator = iterator;
			this.key = key;
		}

		@Override
		public boolean hasNext()
		{
			return iterator.hasNext();
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public DataPoint<T> next()
		{
			Entry entry = iterator.next();
			return new DataPointImpl<T>( entry.getTimestamp(), ( T )entry.getValue( key ) );
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}