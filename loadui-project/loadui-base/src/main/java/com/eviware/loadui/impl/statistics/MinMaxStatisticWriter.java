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

import java.util.Map;
import java.util.TreeMap;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;

/**
 * 
 * @author robert
 *
 * MinMax Writer, keep minimum and maximum value of observed StatisticVariable.
 *
 */
public class MinMaxStatisticWriter extends AbstractStatisticsWriter
{

	public static final String TYPE = "MINMAX";

	public enum Stats
	{
		MIN, MAX;
	}

	protected Double minimum;
	protected Double maximum;

	public MinMaxStatisticWriter( StatisticsManager manager, StatisticVariable variable,
			Map<String, Class<? extends Number>> values )
	{
		super( manager, variable, values );
	}

	@Override
	protected String getType()
	{
		return TYPE;
	}

	@Override
	public void flush()
	{
		lastTimeFlushed = System.currentTimeMillis();
		at( lastTimeFlushed ).put( Stats.MAX.name(), maximum ).put( Stats.MIN.name(), minimum ).write();
	}

	@Override
	public int getValueCount()
	{
		return 1;
	}

	@Override
	protected void reset()
	{
		minimum = 0d;
		maximum = 0d;
	}
	/*
	 * flash() will be called only when timeperiod is expired and min or max
	 * value is changed. This is done so save space in database. (non-Javadoc)
	 * 
	 * @see com.eviware.loadui.api.statistics.StatisticsWriter#update(long,
	 * java.lang.Number[])
	 */
	@Override
	public void update( long timestamp, Number... values )
	{
		if( values.length < 1 )
			return;
		boolean dirty = false;
		if( minimum == null || minimum > ( Double )values[0] )
		{
			minimum = ( Double )values[0];
			dirty = true; 
		}
		if( maximum == null || maximum < ( Double )values[0] )
		{
			maximum = ( Double )values[0];
			dirty = true;
		}
		if( lastTimeFlushed + delay >= System.currentTimeMillis() && dirty )
			flush();
	}

	/**
	 * Factory for instantiating MinMaxStatisticWriters.
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
		public StatisticsWriter createStatisticsWriter( StatisticsManager statisticsManager, StatisticVariable variable )
		{
			Map<String, Class<? extends Number>> trackStructure = new TreeMap<String, Class<? extends Number>>();

			trackStructure.put( Stats.MAX.name(), Long.class );
			trackStructure.put( Stats.MIN.name(), Long.class);
			
			return new MinMaxStatisticWriter( statisticsManager, variable, trackStructure );
		}
	}

}
