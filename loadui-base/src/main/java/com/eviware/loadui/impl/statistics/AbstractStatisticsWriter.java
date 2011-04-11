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

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.eviware.loadui.util.statistics.store.TrackDescriptorImpl;

public abstract class AbstractStatisticsWriter implements StatisticsWriter, Releasable
{
	public final static Logger log = LoggerFactory.getLogger( AbstractStatisticsWriter.class );

	public static final String DELAY = "delay";
	public static final String NAMES = "names";

	private final StatisticsManager manager;
	private final StatisticVariable variable;
	private final String id;
	private final TrackDescriptor descriptor;

	protected long delay;
	protected long lastTimeFlushed = System.currentTimeMillis();

	private long[] aggregateIntervals = { 6000, // 6 seconds
			240000, // 4 minutes
			7200000, // 2 hours
			43200000 // 12 hours
	};
	private AggregateLevel[] aggregateLevels = new AggregateLevel[4];
	private HashSet<Entry> firstLevelEntries = new HashSet<Entry>();

	private final Map<String, Object> config;

	public AbstractStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
			Map<String, Class<? extends Number>> values, Map<String, Object> config )
	{
		this.config = config;
		this.manager = manager;
		this.variable = variable;
		id = DigestUtils.md5Hex( variable.getStatisticHolder().getId() + variable.getName() + getType() );
		descriptor = new TrackDescriptorImpl( id, values );
		delay = config.containsKey( DELAY ) ? ( ( Number )config.get( DELAY ) ).longValue() : manager
				.getMinimumWriteDelay();

		// init AggregationLevels, each level getting a reference to the previous
		// level's aggregated entries.
		HashSet<Entry> previousLevelEntries = firstLevelEntries;
		for( int i = 0; i < aggregateLevels.length; i++ )
		{
			aggregateLevels[i] = new AggregateLevel( aggregateIntervals[i], i + 1, previousLevelEntries );
			previousLevelEntries = aggregateLevels[i].aggregatedEntries;
		}

		// TODO
		manager.getExecutionManager().registerTrackDescriptor( descriptor );
	}

	protected Map<String, Object> getConfig()
	{
		return config;
	}

	/**
	 * Called between Executions, letting the StatisticsWriter know that it
	 * should clear any buffers and prepare for a new Execution.
	 */
	protected void reset()
	{
		lastTimeFlushed = System.currentTimeMillis();

		for( AggregateLevel aggregateLevel : aggregateLevels )
			aggregateLevel.flush();
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public StatisticVariable getStatisticVariable()
	{
		return variable;
	}

	@Override
	public TrackDescriptor getTrackDescriptor()
	{
		return descriptor;
	}

	@Override
	public void flush()
	{
		ExecutionManager executionManager = manager.getExecutionManager();

		Entry e = output();
		if( e != null )
		{
			executionManager.writeEntry( id, e, StatisticVariable.MAIN_SOURCE, 0 );
			firstLevelEntries.add( e );
		}

		for( AggregateLevel a : aggregateLevels )
		{
			if( !a.needsFlushing() )
				break;
			Entry aggregatedEntry = aggregate( a.sourceEntries, false );
			a.flush();
			if( aggregatedEntry != null )
			{
				a.aggregatedEntries.add( aggregatedEntry );
				executionManager.writeEntry( id, aggregatedEntry, StatisticVariable.MAIN_SOURCE, a.getDatabaseKey() );
			}
		}
	}

	@Override
	public void release()
	{
	}

	protected EntryBuilder at( long timestamp )
	{
		return new EntryBuilder( timestamp );
	}

	/**
	 * Builder for use in at( int timestamp ) to make writing data to the proper
	 * Track easy.
	 * 
	 * @author dain.nilsson
	 */
	protected class EntryBuilder
	{
		private final long timestamp;
		private final Map<String, Number> values = new HashMap<String, Number>();

		public EntryBuilder( long timestamp )
		{
			this.timestamp = timestamp;
		}

		public <T extends Number> EntryBuilder put( String name, T value )
		{
			values.put( name, value );
			return this;
		}

		public long getTimestamp()
		{
			return timestamp;
		}

		public Entry build()
		{
			return new EntryImpl( timestamp, values, true );
		}
	}

	private static class AggregateLevel
	{
		private long intervalInMillis;
		private long lastFlush;
		private int databaseKey;
		private HashSet<Entry> sourceEntries;
		final public HashSet<Entry> aggregatedEntries = new HashSet<Entry>();

		AggregateLevel( long intervalInMillis, int databaseKey, HashSet<Entry> sourceEntries )
		{
			this.intervalInMillis = intervalInMillis;
			this.databaseKey = databaseKey;
			this.sourceEntries = sourceEntries;
			lastFlush = System.currentTimeMillis();
		}

		public boolean needsFlushing()
		{
			return lastFlush + intervalInMillis <= System.currentTimeMillis();
		}

		public void flush()
		{
			lastFlush = System.currentTimeMillis();
			sourceEntries.clear();
		}

		private int getDatabaseKey()
		{
			return databaseKey;
		}
	}
}