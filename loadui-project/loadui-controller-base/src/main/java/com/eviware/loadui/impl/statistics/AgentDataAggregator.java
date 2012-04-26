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
package com.eviware.loadui.impl.statistics;

import java.util.NavigableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.statistics.EntryAggregator;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsAggregator;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.ExecutionListener;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionManager.State;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

public class AgentDataAggregator implements StatisticsAggregator
{
	public final static Logger log = LoggerFactory.getLogger( AgentDataAggregator.class );

	private final static int BUFFER_SIZE = 5;

	private final NavigableMap<Long, SetMultimap<String, Entry>> times = Maps.newTreeMap();
	private final ExecutionManager executionManager;
	private final StatisticsInterpolator statisticsInterpolator;

	public AgentDataAggregator( ExecutionManager executionManager )
	{
		this.executionManager = executionManager;
		statisticsInterpolator = new StatisticsInterpolator( executionManager );

		executionManager.addExecutionListener( new FlushingExecutionListener() );
	}

	public synchronized void update( Entry entry, String trackId, AgentItem agent )
	{
		statisticsInterpolator.update( entry, trackId, agent.getLabel() );

		long time = entry.getTimestamp() / 1000;

		if( !times.containsKey( time ) )
		{
			times.put( time, HashMultimap.<String, Entry> create() );
			if( times.size() > BUFFER_SIZE )
			{
				java.util.Map.Entry<Long, SetMultimap<String, Entry>> oldestEntry = times.pollFirstEntry();
				if( oldestEntry.getKey().equals( time ) )
				{
					// log.debug( "Received expired Entry: {} from: {}", entry,
					// agent.getLabel() );
					return;
				}
				flush( oldestEntry.getValue() );
			}
		}
		times.get( time ).put( trackId, entry );
	}

	private synchronized void flush( SetMultimap<String, Entry> map )
	{
		for( String trackId : map.keySet() )
		{
			Track track = executionManager.getTrack( trackId );
			if( track == null )
			{
				continue;
			}

			EntryAggregator aggregator = track.getTrackDescriptor().getEntryAggregator();

			if( aggregator != null )
			{
				Entry entry = aggregator.aggregate( map.get( trackId ), true );

				if( entry != null )
					statisticsInterpolator.update( entry, trackId, StatisticVariable.MAIN_SOURCE );
			}
		}
	}

	private void flushAll()
	{
		long flushTime = System.currentTimeMillis();
		while( !times.isEmpty() )
			flush( times.pollFirstEntry().getValue() );
		statisticsInterpolator.flush( flushTime );
	}

	@Override
	public void addEntry( String trackId, Entry entry )
	{
		statisticsInterpolator.update( entry, trackId, StatisticVariable.MAIN_SOURCE );
	}

	@Override
	public void addEntry( String trackId, Entry entry, String source )
	{
		statisticsInterpolator.update( entry, trackId, source );
	}

	private class FlushingExecutionListener implements ExecutionListener
	{
		@Override
		public void executionStarted( State oldState )
		{
			times.clear();
			statisticsInterpolator.reset();
		}

		@Override
		public void executionPaused( State oldState )
		{
		}

		@Override
		public void executionStopped( State oldState )
		{
			flushAll();
		}

		@Override
		public void trackRegistered( TrackDescriptor trackDescriptor )
		{
		}

		@Override
		public void trackUnregistered( TrackDescriptor trackDescriptor )
		{
		}
	}
}
