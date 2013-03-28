/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.statistics;

import java.util.Collections;

import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.util.StringUtils;
import com.eviware.loadui.util.serialization.ListenableValueSupport;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class StatisticImpl<T extends Number> implements Statistic<T>
{
	private final ListenableValueSupport<T> listenableValueSupport = new ListenableValueSupport<>();
	private final EntryListener entryListener = new EntryListener();
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
		Entry lastEntry = manager.getLastEntry( trackId, source );
		return lastEntry == null ? null : ( T )lastEntry.getValue( name );
	}

	@Override
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public DataPoint<T> getLatestPoint( int interpolationLevel )
	{
		Entry lastEntry = manager.getLastEntry( trackId, source, interpolationLevel );
		return lastEntry == null ? null : new DataPointImpl( lastEntry.getTimestamp(), lastEntry.getValue( name ) );
	}

	@Override
	public String getLabel()
	{
		return name;
	}

	@Override
	public StatisticVariable getStatisticVariable()
	{
		return variable;
	}

	@Override
	public String getSource()
	{
		return source;
	}

	@Override
	public Iterable<DataPoint<T>> getPeriod( long start, long end, int interpolationLevel, Execution execution )
	{
		if( execution == null )
			return Collections.emptyList();

		Track track = execution.getTrack( trackId );
		if( track == null )
			return Collections.emptyList();

		return Iterables.transform( track.getRange( source, start, end, interpolationLevel ),
				new Function<Entry, DataPoint<T>>()
				{
					@Override
					@SuppressWarnings( "unchecked" )
					public DataPoint<T> apply( Entry entry )
					{
						return new DataPointImpl<>( entry.getTimestamp(), ( T )entry.getValue( name ) );
					}
				} );
	}

	@Override
	public Iterable<DataPoint<T>> getPeriod( long start, long end, int interpolationLevel )
	{
		return getPeriod( start, end, interpolationLevel, manager.getCurrentExecution() );
	}

	@Override
	public Iterable<DataPoint<T>> getPeriod( long start, long end )
	{
		return getPeriod( start, end, 0, manager.getCurrentExecution() );
	}

	@Override
	public long getTimestamp()
	{
		Entry lastEntry = manager.getLastEntry( trackId, source );
		return lastEntry == null ? -1 : lastEntry.getTimestamp();
	}

	@Override
	public void addListener( ListenableValue.ValueListener<? super T> listener )
	{
		listenableValueSupport.addListener( listener );
		manager.addEntryListener( trackId, source, 0, entryListener );
	}

	@Override
	public void removeListener( ListenableValue.ValueListener<? super T> listener )
	{
		listenableValueSupport.removeListener( listener );
		if( listenableValueSupport.getListenerCount() == 0 )
		{
			manager.removeEntryListener( trackId, source, 0, entryListener );
		}
	}

	@Override
	public String toString()
	{
		return String.format( "%s > %s%s", StringUtils.capitalize( getStatisticVariable().getLabel() ),
				StringUtils.capitalize( name ), StatisticVariable.MAIN_SOURCE.equals( source ) ? "" : "(" + source + ")" );
	}

	private class EntryListener implements ValueListener<Entry>
	{
		@Override
		@SuppressWarnings( "unchecked" )
		public void update( Entry value )
		{
			if( listenableValueSupport.getListenerCount() > 0 )
			{
				listenableValueSupport.update( ( T )value.getValue( name ) );
			}
			else
			{
				manager.removeEntryListener( trackId, source, 0, this );
			}
		}
	}
}
