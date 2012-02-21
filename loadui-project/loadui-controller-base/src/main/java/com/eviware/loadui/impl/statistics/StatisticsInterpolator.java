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

import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionManager.State;
import com.eviware.loadui.api.statistics.store.Track;

public class StatisticsInterpolator
{
	public final static Logger log = LoggerFactory.getLogger( StatisticsInterpolator.class );

	private static final long[] aggregateIntervals = { 6000, // 6 seconds
			10 * 60 * 1000, // 10 minutes
			2 * 60 * 60 * 1000, // 2 hours
			12 * 60 * 60 * 1000 // 12 hours
	};

	private final ExecutionManager executionManager;

	private final HashMap<String, AggregateLevel> aggregateLevels = new HashMap<String, AggregateLevel>();

	public StatisticsInterpolator( ExecutionManager executionManager )
	{
		this.executionManager = executionManager;
	}

	public synchronized void reset()
	{
		aggregateLevels.clear();
	}

	public synchronized void update( Entry entry, String trackId, String source )
	{
		long currentTime = entry.getTimestamp();
		if( executionManager.getState() != State.STOPPED )
		{
			executionManager.writeEntry( trackId, entry, source );

			String key = trackId + source;
			if( !aggregateLevels.containsKey( key ) )
				aggregateLevels.put( key, new AggregateLevel( source, trackId, 0, currentTime ) );

			aggregateLevels.get( key ).update( entry, currentTime );
		}
	}

	public synchronized void flush( long flushTime )
	{
		if( executionManager.getState() != State.STOPPED )
		{
			for( java.util.Map.Entry<String, AggregateLevel> entry : aggregateLevels.entrySet() )
			{
				String source = entry.getKey();
				AggregateLevel sublevel = entry.getValue();
				while( sublevel != null )
				{
					Entry aggregateEntry = sublevel.flush( flushTime );
					if( aggregateEntry != null )
					{
						executionManager.writeEntry( sublevel.trackId, aggregateEntry, source, sublevel.level + 1 );
						if( sublevel.child != null )
							sublevel.child.update( aggregateEntry, flushTime );
					}
					sublevel = sublevel.child;
				}
			}
		}
	}

	private class AggregateLevel
	{
		private final String source;
		private final String trackId;
		private final int level;
		private final AggregateLevel child;
		private long lastFlush;
		private final HashSet<Entry> entries = new HashSet<Entry>();

		private AggregateLevel( String source, String trackId, int level, long currentTime )
		{
			this.source = source;
			this.trackId = trackId;
			this.level = level;
			lastFlush = currentTime;

			child = level < aggregateIntervals.length - 1 ? new AggregateLevel( source, trackId, level + 1, currentTime )
					: null;
		}

		public void update( Entry entry, long currentTime )
		{
			long flushTime = lastFlush + aggregateIntervals[level];
			Entry aggregateEntry = null;

			if( entry.getTimestamp() <= flushTime && currentTime >= flushTime )
			{
				entries.add( entry );
				aggregateEntry = flush( flushTime );
			}
			else if( currentTime >= flushTime )
			{
				aggregateEntry = flush( flushTime );
				entries.add( entry );
			}
			else
			{
				entries.add( entry );
			}

			if( aggregateEntry != null )
			{
				executionManager.writeEntry( trackId, aggregateEntry, source, level + 1 );
				if( child != null )
					child.update( entry, currentTime );
			}
		}

		/**
		 * @param flushTime
		 * @return
		 */
		private Entry flush( long flushTime )
		{
			lastFlush = flushTime;

			Track track = executionManager.getTrack( trackId );
			if( entries.isEmpty() || track == null )
				return null;

			Entry entry = track.getTrackDescriptor().getEntryAggregator().aggregate( entries, false );
			entries.clear();

			return entry;
		}
	}
}