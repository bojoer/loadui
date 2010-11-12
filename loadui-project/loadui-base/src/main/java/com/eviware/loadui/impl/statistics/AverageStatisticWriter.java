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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;

import org.apache.commons.math.stat.descriptive.rank.Percentile;

/**
 * StatisticsWriter for calculating the average of given values.
 * 
 * @author dain.nilsson
 */
public class AverageStatisticWriter extends AbstractStatisticsWriter
{
	public static final String TYPE = "AVERAGE";

	private Percentile perc = new Percentile( 90 );

	public enum Stats
	{
		AVERAGE,
		AVERAGE_COUNT,
		AVERAGE_SUM, 
		STD_DEV ,
		STD_DEV_SUM, 
		PERCENTILE;

	}

	/**
	 * Average = Average_Sum / Average_Count
	 * 
	 * Where is:
	 * * Average_Sum is sum of all requests times ( total or range )
	 * * Average_Count is total number of requests ( total or range )
	 * 
	 * Standard_Deviation = Square_Sum / Average_Count Where
	 * 
	 * Where is:
	 * *Square_Sum =Math.pow( timeTaken - Average_Sum, 2 ) 
	 * *timeTaken is last request time taken
	 * 
	 * For calculating 90 percentile it needed to remember data received.
	 * To do this is defined buffer which hold last n values. Default value is 1000, but this 
	 * can be changed by set/getPercentileBufferSize. Since percentile is expensive operation, specially
	 * when buffer is large, it should be calculated just before it should be written to database.
	 */

	protected double average = 0L;
	protected long avgSum = 0L;
	protected long avgCnt = 0;
	protected double stdDev = 0.0;
	protected double sumTotalSquare = 0.0;
	protected double percentile = 0;

	private int percentileBufferSize = 1000;

	private ArrayList<Double> values = new ArrayList<Double>();

	public AverageStatisticWriter( StatisticsManager statisticsManager, StatisticVariable variable, Map<String, Class<? extends Number>> trackStructure )
	{
		super( statisticsManager, variable, trackStructure );
	}

	@Override
	protected String getType()
	{
		return TYPE;
	}

	@Override
	public int getValueCount()
	{
		return 1;
	}

	/**
	 * values : [ value:long ]
	 */
	@Override
	public void update( long timestamp, Number... values )
	{
		if ( values.length < 1 ) 
			return;
		this.values.add( ( Double )values[0] );
		if( this.values.size() >= percentileBufferSize )
			this.values.remove( 0 );
		avgSum += ( Double )values[0];
		avgCnt++ ;
		sumTotalSquare += Math.pow( ( Double )values[0] - avgSum, 2 );
		stdDev = sumTotalSquare / avgCnt;
		if( lastTimeFlushed + delay >= System.currentTimeMillis() )
			flush();
	}

	/**
	 * stores data in db. Also here calculate percentile since it is expensive calculation
	 */
	@Override
	public void flush()
	{
		// calculate percentile here since it is expensive operation.
		double[] pValues = new double[values.size()];
		for( int cnt = 0; cnt < values.size(); cnt++ )
			pValues[cnt] = values.get( cnt ).longValue();
		percentile = perc.evaluate( pValues );
		lastTimeFlushed = System.currentTimeMillis();
		average = avgSum / avgCnt;
		
		at( lastTimeFlushed ).put( Stats.AVERAGE.name(), average )
									.put( Stats.AVERAGE_COUNT.name(), avgCnt)
									.put( Stats.AVERAGE_SUM.name(), avgSum )
									.put( Stats.STD_DEV_SUM.name(), sumTotalSquare )
									.put( Stats.STD_DEV.name(), stdDev )
									.put( Stats.PERCENTILE.name(), percentile).write();
	}

	@Override
	protected void reset()
	{
		average = 0L;
		avgSum = 0L;
		avgCnt = 0;
		stdDev = 0.0;
		sumTotalSquare = 0.0;
		percentile = 0;
	}
	
	public int getPercentileBufferSize()
	{
		return percentileBufferSize;
	}

	public void setPercentileBufferSize( int percentileBufferSize )
	{
		this.percentileBufferSize = percentileBufferSize;
	}

	/**
	 * Factory for instantiating AverageStatisticWriters.
	 * 
	 * @author dain.nilsson
	 * 
	 * Define what Statistics this writer should write in Track.
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

			trackStructure.put( Stats.AVERAGE.name(), Double.class );
			trackStructure.put( Stats.AVERAGE_SUM.name(), Long.class );
			trackStructure.put( Stats.AVERAGE_COUNT.name(), Long.class );
			trackStructure.put( Stats.STD_DEV.name(), Double.class );
			trackStructure.put( Stats.STD_DEV_SUM.name(), Double.class );
			trackStructure.put( Stats.PERCENTILE.name(), Double.class );
			
			return new AverageStatisticWriter( statisticsManager, variable, trackStructure );
		}
	}
}