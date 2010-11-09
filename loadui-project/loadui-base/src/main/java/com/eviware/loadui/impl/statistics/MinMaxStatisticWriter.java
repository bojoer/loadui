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

public class MinMaxStatisticWriter extends AbstractStatisticsWriter
{

	public static final String TYPE = "MINMAX";

	public enum Stats
	{
		MIN( "Minimum" ), MAX( "Maximum" );

		private final String name;

		Stats( String name )
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}

	private long lastTimeFlashed;
	private Double minimum;
	private Double maximum;

	public MinMaxStatisticWriter( StatisticVariable variable )
	{
		super( variable );

		statisticNames.put( Stats.MIN.getName(), Double.class );
		statisticNames.put( Stats.MAX.getName(), Double.class );

	}

	@Override
	protected String getType()
	{
		return TYPE;
	}

	@Override
	public void flush()
	{
		lastTimeFlashed = System.currentTimeMillis();
	}

	@Override
	public int getValueCount()
	{
		return 1;
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
		boolean dirty = false;
		if( minimum > ( Double )values[0] )
		{
			minimum = ( Double )values[0];
			dirty = true;
		}
		if( maximum < ( Double )values[0] )
		{
			maximum = ( Double )values[0];
			dirty = true;
		}
		if( lastTimeFlashed + delay >= System.currentTimeMillis() && dirty )
			flush();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Number getStatisticValue( String statisticName, String instance )
	{
		switch( Stats.valueOf( statisticName ) )
		{
		case MIN :
			return minimum;
		case MAX :
			return maximum;
		default :
			return null;
		}
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
			return new MinMaxStatisticWriter( variable );
		}
	}

}
