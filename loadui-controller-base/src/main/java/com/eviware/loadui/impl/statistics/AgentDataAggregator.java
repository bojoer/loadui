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
package com.eviware.loadui.impl.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsAggregator;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.ExecutionListener;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionManager.State;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;

public class AgentDataAggregator implements StatisticsAggregator
{
	public final static Logger log = LoggerFactory.getLogger( AgentDataAggregator.class );

	private final static int BUFFER_SIZE = 5;

	private final TreeMap<Long, Map<String, Set<Entry>>> times = new TreeMap<Long, Map<String, Set<Entry>>>();
	private final AddressableRegistry addressableRegistry;
	private final StatisticsInterpolator statisticsInterpolator;

	public AgentDataAggregator( ExecutionManager executionManager, AddressableRegistry addressableRegistry )
	{
		this.addressableRegistry = addressableRegistry;
		statisticsInterpolator = new StatisticsInterpolator( executionManager, addressableRegistry );

		executionManager.addExecutionListener( new FlushingExecutionListener() );
	}

	public synchronized void update( Entry entry, String trackId, AgentItem agent )
	{
		statisticsInterpolator.update( entry, trackId, agent.getLabel() );

		long time = entry.getTimestamp() / 1000;

		if( !times.containsKey( time ) )
		{
			times.put( time, new HashMap<String, Set<Entry>>() );
			if( times.size() > BUFFER_SIZE )
			{
				java.util.Map.Entry<Long, Map<String, Set<Entry>>> oldestEntry = times.pollFirstEntry();
				if( oldestEntry.getKey().equals( time ) )
				{
					// log.debug( "Received expired Entry: {} from: {}", entry,
					// agent.getLabel() );
					return;
				}
				flush( oldestEntry.getValue() );
			}
		}
		Map<String, Set<Entry>> tracks = times.get( time );

		if( !tracks.containsKey( trackId ) )
			tracks.put( trackId, new HashSet<Entry>() );
		Set<Entry> entries = tracks.get( trackId );

		entries.add( entry );
	}

	private synchronized void flush( Map<String, Set<Entry>> map )
	{
		for( Map.Entry<String, Set<Entry>> e : map.entrySet() )
		{
			String trackId = e.getKey();
			StatisticsWriter writer = ( StatisticsWriter )addressableRegistry.lookup( trackId );

			if( writer != null && !writer.getType().equals( "COUNTER" ) )
			{
				Entry entry = writer.aggregate( e.getValue(), true );

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
