package com.eviware.loadui.impl.statistics;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;
import com.eviware.loadui.api.statistics.store.Entry;

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

	public static enum Stats
	{
		VALUE
	}

	private double sum = 0;
	private double lastValue = Double.NaN;
	private long lastUpdate = System.currentTimeMillis();

	public VariableStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
			Map<String, Class<? extends Number>> values, Map<String, Object> config )
	{
		super( manager, variable, values, config );
	}

	@Override
	public void update( long timestamp, Number value )
	{
		synchronized( this )
		{
			if( lastValue != Double.NaN )
			{
				while( lastTimeFlushed + delay < timestamp )
					flush();
				long delta = timestamp - lastUpdate;
				sum += lastValue * delta;
			}
			else
			{
				lastTimeFlushed = timestamp;
			}
			lastUpdate = timestamp;
			lastValue = value.doubleValue();
		}
	}

	@Override
	public Entry output()
	{
		double value = sum / delay;
		lastTimeFlushed += delay;
		sum = 0;
		if( lastUpdate < lastTimeFlushed )
			lastUpdate = lastTimeFlushed;

		return at( lastTimeFlushed ).put( Stats.VALUE.name(), value ).build();
	}

	@Override
	public Entry aggregate( Set<Entry> entries, boolean parallel )
	{
		if( entries.size() <= 1 )
			return entries.size() == 0 ? null : entries.iterator().next();

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

	@Override
	protected void reset()
	{
		super.reset();

		lastUpdate = lastTimeFlushed;
		lastValue = Double.NaN;
		sum = 0;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	public static class Factory implements StatisticsWriterFactory
	{
		@Override
		public String getType()
		{
			return TYPE;
		}

		@Override
		public StatisticsWriter createStatisticsWriter( StatisticsManager statisticsManager, StatisticVariable variable,
				Map<String, Object> config )
		{
			return new VariableStatisticsWriter( statisticsManager, variable,
					Collections.<String, Class<? extends Number>> singletonMap( Stats.VALUE.name(), Double.class ), config );
		}
	}
}
