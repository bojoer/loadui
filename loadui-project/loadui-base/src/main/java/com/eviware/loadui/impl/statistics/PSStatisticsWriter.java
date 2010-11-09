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

public class PSStatisticsWriter extends AbstractStatisticsWriter
{

	private final static String TYPE = "PSWritter";
	private long lastTimeFlashed;
	private Double perSecond;
	
	//cound not find better name
	public enum Stats
	{
		PS( "Per_Second" );

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
	
	public PSStatisticsWriter(StatisticVariable variable)
	{
		super( variable );
		
		statisticNames.put( Stats.PS.getName(), Double.class );
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
		//TODO: write to DB
	}

	/**
	 * this writer needs number of operations/transfers/bytes and length of time in which they happend.
	 */
	@Override
	public int getValueCount()
	{
		return 2;
	}

	@Override
	public void update( long timestamp, Number... values )
	{
		// ignore data if there is less than 2 or second one is 0
		if ( values.length < 2 ) 
			return;
		if ( (Double)values[1] == 0 )
			return;
		perSecond = (Double)values[0] / (Double)values[1];
		
		if( lastTimeFlashed + delay >= System.currentTimeMillis() )
			flush();
	}
	
	@Override
	public Double getStatisticValue( String statisticName, String instance )
	{
		return perSecond;
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
