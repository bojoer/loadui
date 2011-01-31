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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;
import com.eviware.loadui.api.statistics.store.Entry;

/**
 * 
 * Calculate changes in time like BPS or TPS. These values are calculated at the
 * end of period when writing to database occurs.
 * 
 * Also, provides Statistic Value which shows last second change. This values is
 * calculated when ever update occurs.
 * 
 * PS Statistics is per second statistic during whole run of test.
 * LAST_SECOND_CHANGE is a per second change in last second.
 */
public class ThroughputStatisticsWriter extends AbstractStatisticsWriter
{
	public final static String TYPE = "THROUGHPUT";

	private int count = 0;
	private double sum = 0;

	public enum Stats
	{
		BPS, TPS;
	}

	public ThroughputStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
			Map<String, Class<? extends Number>> values )
	{
		super( manager, variable, values );
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public Entry output()
	{
		double timeDelta = delay / 1000.0;
		double bps = sum / timeDelta;
		double tps = count / timeDelta;
		sum = 0;
		count = 0;
		lastTimeFlushed += delay;

		Entry e = at( lastTimeFlushed ).put( Stats.BPS.name(), bps ).put( Stats.TPS.name(), tps ).build();

		return e;
	}

	/**
	 * this writer needs number of operations/transfers/bytes
	 */
	@Override
	public int getValueCount()
	{
		return 1;
	}

	@Override
	public void update( long timestamp, Number value )
	{
		synchronized( this )
		{
			while( lastTimeFlushed + delay < timestamp )
				flush();

			count++ ;
			sum += value.doubleValue();
		}
	}

	@Override
	public Entry aggregate( List<Entry> entries )
	{
		if( entries.size() <= 1 )
			return entries.size() == 0 ? null : entries.get( 0 );

		double tpsSum = 0;
		double bpsSum = 0;
		long minTime = Long.MAX_VALUE;
		long maxTime = 0;
		for( Entry entry : entries )
		{
			bpsSum += entry.getValue( Stats.BPS.name() ).doubleValue();
			tpsSum += entry.getValue( Stats.TPS.name() ).doubleValue();
			minTime = Math.min( minTime, entry.getTimestamp() );
			maxTime = Math.max( maxTime, entry.getTimestamp() );
		}

		double timeDelta = Math.max(
				( ( double )( maxTime - minTime ) / entries.size() * ( entries.size() + 1 ) ) / 1000, delay / 1000.0 );

		Entry e = at( maxTime ).put( Stats.BPS.name(), bpsSum / timeDelta ).put( Stats.TPS.name(), tpsSum / timeDelta )
				.build( false );

		return e;
	}

	@Override
	protected void reset()
	{
		sum = 0;
		count = 0;

		lastTimeFlushed = System.currentTimeMillis();
	}

	/**
	 * Factory for instantiating ThroughputStatisticsWriters.
	 * 
	 * 
	 */
	public static class Factory implements StatisticsWriterFactory
	{
		@Override
		public String getType()
		{
			return TYPE;
		}

		@Override
		public ThroughputStatisticsWriter createStatisticsWriter( StatisticsManager statisticsManager,
				StatisticVariable variable )
		{
			Map<String, Class<? extends Number>> trackStructure = new TreeMap<String, Class<? extends Number>>();

			// init statistics

			trackStructure.put( Stats.BPS.name(), Double.class );
			trackStructure.put( Stats.TPS.name(), Double.class );

			return new ThroughputStatisticsWriter( statisticsManager, variable, trackStructure );
		}
	}
}
