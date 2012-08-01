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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.eviware.loadui.api.statistics.EntryAggregator;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;
import com.eviware.loadui.api.statistics.store.Entry;
import com.google.common.collect.Iterables;

/**
 * A StatisticsWriter used to calculate a raw value, where each update signifies
 * a change in the value, and the time between updates is thus important in the
 * calculation.
 * 
 * @author dain.nilsson
 */
public class VariableStatisticsWriter extends AbstractStatisticsWriter
{
	public static final String TYPE = "VARIABLE";

	public enum Stats
	{
		VALUE( "The number of %v." );

		private final String description;

		Stats()
		{
			this.description = this.name() + " of %v.";
		}

		Stats( String description )
		{
			this.description = description;
		}
	}

	private double sum = 0;
	private double lastValue = Double.NaN;
	private long lastUpdate = System.currentTimeMillis();

	public VariableStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
			Map<String, Class<? extends Number>> values, Map<String, Object> config )
	{
		super( manager, variable, values, config, new Aggregator() );
	}

	@Override
	public void update( long timestamp, Number value )
	{
		synchronized( this )
		{
			if( Double.isNaN( lastValue ) )
			{
				lastTimeFlushed = timestamp;
			}
			else
			{
				while( lastTimeFlushed + delay < timestamp )
					flush();

				sum += lastValue * ( timestamp - lastUpdate );
			}

			lastUpdate = timestamp;
			lastValue = value.doubleValue();
		}
	}

	@Override
	public synchronized Entry output()
	{
		sum += ( lastTimeFlushed + delay - lastUpdate ) * lastValue;
		double value = sum / delay;

		lastTimeFlushed += delay;
		sum = 0;
		if( lastUpdate < lastTimeFlushed )
			lastUpdate = lastTimeFlushed;

		return at( lastTimeFlushed ).put( Stats.VALUE.name(), value ).build();
	}

	@Override
	public void reset()
	{
		super.reset();

		synchronized( this )
		{
			lastUpdate = lastTimeFlushed;
			lastValue = Double.NaN;
			sum = 0;
		}
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	private static class Aggregator implements EntryAggregator
	{
		@Override
		public Entry aggregate( Set<Entry> entries, boolean parallel )
		{
			if( entries.size() <= 1 )
				return Iterables.getFirst( entries, null );

			long maxTime = -1;
			double value = 0;
			for( Entry entry : entries )
			{
				maxTime = Math.max( maxTime, entry.getTimestamp() );
				value += entry.getValue( Stats.VALUE.name() ).doubleValue();
			}

			if( !parallel )
				value /= entries.size();

			return at( maxTime ).put( Stats.VALUE.name(), value ).build();
		}
	}

	public static class Factory implements StatisticsWriterFactory
	{
		@Override
		public String getType()
		{
			return TYPE;
		}

		@Override
		public VariableStatisticsWriter createStatisticsWriter( StatisticsManager statisticsManager,
				StatisticVariable variable, Map<String, Object> config )
		{
			return new VariableStatisticsWriter( statisticsManager, variable,
					Collections.<String, Class<? extends Number>> singletonMap( Stats.VALUE.name(), Double.class ), config );
		}
	}

	@Override
	public String getDescriptionForMetric( String metricName )
	{
		for( Stats s : Stats.values() )
		{
			if( s.name().equals( metricName ) )
				return s.description;
		}
		return null;
	}
}
