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
 */
public class PSStatisticsWriter extends AbstractStatisticsWriter
{

	private final static String TYPE = "PSWritter";

	private long lastTimeFlashed;
	private Double perSecond;
	private Double totalSum;

	private long lastTimeUpdated;

	private double lastSecondChange;

	//cound not find better name
	public enum Stats
	{
		PS, LAST_SECOND_CHANGE;

	}

	public PSStatisticsWriter( StatisticVariable variable )
	{
		super( variable );

		statisticNames.put( Stats.PS.name(), Double.class );
		statisticNames.put( Stats.LAST_SECOND_CHANGE.name(), Double.class );
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
		perSecond = totalSum / ( (System.currentTimeMillis() - lastTimeFlashed) / 1000 );
		totalSum = 0D;
		lastTimeFlashed = System.currentTimeMillis();
		//TODO: write to DB
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
		totalSum += ( Double )values[0];
		lastSecondChange = ( Double )values[0] / ( ( System.currentTimeMillis() - lastTimeUpdated ) / 1000 );
		lastTimeUpdated = System.currentTimeMillis();

		if( lastTimeFlashed + delay >= System.currentTimeMillis() )
			flush();
	}

	@Override
	public Double getStatisticValue( String statisticName, String instance )
	{
		switch( Stats.valueOf( statisticName ) )
		{
		case PS :
			return perSecond;
		case LAST_SECOND_CHANGE:
			return lastSecondChange;
		default :
			return null;
		}
	}

	/**
	 * Factory for instantiating PSStatisticWriters.
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
			return new PSStatisticsWriter( variable );
		}
	}
}
