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
 * Calculate changes in time like BPS or TPS. These values are calculated 
 * at the end of period when writing to database occurs. 
 * 
 * Also, provides Statistic Value which shows last second change. This values
 * is calculated when ever update occurs. 
 * 
 * PS Statistics is per second statistic during whole run of test.
 * LAST_SECOND_CHANGE is a per second change in last second.
 */
public class PSStatisticsWriter extends AbstractStatisticsWriter
{

	private final static String TYPE = "PSWritter";

	private Double perSecond;
	private Double totalSum;

	private long lastTimeUpdated = System.currentTimeMillis();

	private double lastSecondChange;

	//cound not find better name
	public enum Stats
	{
		PS, LAST_SECOND_CHANGE;

	}

	public PSStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
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
		// it should be per second
		perSecond = totalSum / ( ( System.currentTimeMillis() - lastTimeFlushed ) / 1000 );
		totalSum = 0D;
		lastTimeFlushed = System.currentTimeMillis();
		at( lastTimeFlushed ).put( Stats.PS.name(), perSecond )
									.put( Stats.LAST_SECOND_CHANGE.name(), lastSecondChange )
									.write();;
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
	public void update( long timestamp, Number... values )
	{
		// ignore data if there is less than 1 
		if( values.length < 1 )
			return;
		totalSum += values[0].doubleValue();
		lastSecondChange = values[0].doubleValue() / ( ( System.currentTimeMillis() - lastTimeUpdated ) / 1000 );
		lastTimeUpdated = System.currentTimeMillis();

		if( lastTimeFlushed + delay >= System.currentTimeMillis() )
			flush();
	}

	@Override
	protected void reset()
	{
		perSecond = 0d;
		totalSum = 0d;

		lastTimeUpdated = System.currentTimeMillis();

		lastSecondChange = 0d;
	}
	/**
	 * Factory for instantiating PSStatisticWriters.
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
		public StatisticsWriter createStatisticsWriter( StatisticsManager statisticsManager, StatisticVariable variable )
		{
			Map<String, Class<? extends Number>> trackStructure = new TreeMap<String, Class<? extends Number>>();

			// init statistics

			trackStructure.put( Stats.PS.name(), Double.class );
			trackStructure.put( Stats.LAST_SECOND_CHANGE.name(), Double.class);
			
			return new PSStatisticsWriter( statisticsManager, variable, trackStructure );
		}
	}
}
