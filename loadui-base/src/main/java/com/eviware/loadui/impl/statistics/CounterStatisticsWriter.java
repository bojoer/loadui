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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;
import com.eviware.loadui.api.statistics.store.Entry;

public class CounterStatisticsWriter extends AbstractStatisticsWriter
{
	public final static String TYPE = "COUNTER";

	private long total = 0;
	private long change = 0;

	public CounterStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
			Map<String, Class<? extends Number>> values, Map<String, Object> config )
	{
		super( manager, variable, values, config );
	}

	public enum Stats
	{
		TOTAL, PER_SECOND;
	}

	@Override
	public synchronized void update( long timestamp, Number value )
	{
		while( lastTimeFlushed + delay < timestamp )
		{
			flush();
		}
		change += value.longValue();
	}

	@Override
	public Entry output()
	{
		long currentTime = System.currentTimeMillis();
		if( lastTimeFlushed == currentTime )
			return null;
		double timeDelta = delay / 1000.0;
		total += change;
		double perSecond = change / timeDelta;
		change = 0;
		// log.debug( " counterStatWriter:output()   lastTimeFlushed={} delay={}",
		// lastTimeFlushed, delay );
		lastTimeFlushed = Math.min( lastTimeFlushed + delay, currentTime );
		Entry e = at( lastTimeFlushed ).put( Stats.TOTAL.name(), total ).put( Stats.PER_SECOND.name(), perSecond )
				.build();
		// log.debug( " ...resulted in Entry {}",e );
		return e;
	}

	@Override
	public Entry aggregate( Set<Entry> entries, boolean parallel )
	{
		// Counters already aggregate their values on their own, so we shouldn't
		// do this here.
		if( parallel )
			return null;
		if( entries.size() <= 1 )
			return entries.size() == 0 ? null : entries.iterator().next();

		long total = 0;
		double perSecond = 0;
		long maxTime = -1;
		for( Entry entry : entries )
		{
			total = Math.max( total, entry.getValue( Stats.TOTAL.name() ).longValue() );
			perSecond += entry.getValue( Stats.PER_SECOND.name() ).longValue();
			maxTime = Math.max( maxTime, entry.getTimestamp() );
		}

		perSecond /= entries.size();

		return at( maxTime ).put( Stats.TOTAL.name(), total ).put( Stats.PER_SECOND.name(), perSecond ).build();
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	protected void reset()
	{
		super.reset();
		total = 0;
		change = 0;

		lastTimeFlushed = System.currentTimeMillis();
	}

	public static class Factory implements StatisticsWriterFactory
	{
		private final Map<String, Class<? extends Number>> trackStructure = new TreeMap<String, Class<? extends Number>>();

		public Factory()
		{
			trackStructure.put( Stats.TOTAL.name(), Long.class );
			trackStructure.put( Stats.PER_SECOND.name(), Double.class );
		}

		@Override
		public String getType()
		{
			return TYPE;
		}

		@Override
		public StatisticsWriter createStatisticsWriter( StatisticsManager statisticsManager, StatisticVariable variable,
				Map<String, Object> config )
		{
			return new CounterStatisticsWriter( statisticsManager, variable, trackStructure, config );
		}
	}
}
