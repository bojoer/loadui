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
 * StatisticsWriter for calculating the average of given values.
 * 
 * @author dain.nilsson
 */
public class AverageStatisticWriter extends AbstractStatisticsWriter
{
	public static final String TYPE = "AVERAGE";

	// Statistics provided:
//	public static final String AVERAGE = "Average";
//	public static final String AVERAGE_COUNT = "Average_Count";
//	public static final String AVERAGE_SUM = "Average_Sum";
//	public static final String STD_DEV = "Standard_Deviation";
	
	public enum Stats {
		AVERAGE("Average"), 
		AVERAGE_COUNT("Average_Count"), 
		AVERAGE_SUM("Average_Sum"), 
		STD_DEV("Standard_Deviation"),
		STD_DEV_SUM("Standard_Deviation_Sum");
		
		private final String name;
		
		Stats( String name ) {
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
	}
	
	/**
	 * Average = Average_Sum / Average_Count 
	 * 
	 * Where: 
	 * * Average_Sum is sum of all requests times ( total or range ) 
	 * * Average_Count is total number of requests ( total or range )
	 * 
	 * Standard_Deviation = Square_Sum / Average_Count 
	 * Where : 
	 * * Square_Sum =Math.pow( timeTaken - Average_Sum, 2 ) 
	 * * timeTaken is last request time taken
	 * 
	 */

	private long average = 0L;
	private long avgSum = 0L;
	private int avgCnt = 0;
	private double stdDev = 0.0;
	private double sumTotalSquare = 0.0;
	
	

	public AverageStatisticWriter( StatisticVariable variable )
	{
		super( variable );
		// TODO: Create statistics for average, average_count, etc. and add them
		// to
		// the StatisticVariable.
		
		// init statistics
		statisticNames.put( Stats.AVERAGE.getName(), Long.class );
		statisticNames.put( Stats.AVERAGE_SUM.getName(), Long.class );
		statisticNames.put( Stats.AVERAGE_COUNT.getName(), Integer.class );
		statisticNames.put( Stats.STD_DEV.getName(), Double.class );
		statisticNames.put( Stats.STD_DEV_SUM.getName(), Double.class);
		
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
		avgSum  += (Long)values[0];
		avgCnt++;
		average = avgSum / avgCnt;
		sumTotalSquare += Math.pow( (Long)values[0] - avgSum, 2 );
		stdDev = sumTotalSquare / avgCnt;
		//TODO: check if data should be written and call flash
//		flush();
	}
	
	@Override
	public Number getStatisticValue( String statisticName, String instance )
	{
		switch( Stats.valueOf( statisticName ) )
		{
		case AVERAGE :
			return average;
		case STD_DEV :
			return stdDev;
		case AVERAGE_COUNT:
			return avgCnt;
		case AVERAGE_SUM:
			return avgSum;
		case STD_DEV_SUM:
			return sumTotalSquare;
		default :
			return null;
		}
	}

	@Override
	public void flush()
	{
		// TODO Write to the proper Track of the current Execution.

	}

	

	/**
	 * Factory for instantiating AverageStatisticWriters.
	 * 
	 * @author dain.nilsson
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
			return new AverageStatisticWriter( variable );
		}
	}

}