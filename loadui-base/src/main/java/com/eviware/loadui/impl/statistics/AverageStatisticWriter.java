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

import java.util.Collection;
import java.util.Map;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.api.statistics.StatisticsWriterFactory;

/**
 * StatisticsWriter for calculating the average of given values.
 * 
 * @author dain.nilsson
 */
public class AverageStatisticWriter implements StatisticsWriter
{
	public static final String TYPE = "AVERAGE";

	// Statistics provided:
	public static final String AVERAGE = "Average";
	public static final String AVERAGE_COUNT = "Average_Count";

	public AverageStatisticWriter( StatisticsManager statisticsManager, StatisticVariable variable )
	{
		// TODO: Create statistics for average, average_count, etc. and add them
		// to
		// the StatisticVariable.
	}

	@Override
	public int getValueCount()
	{
		return 1;
	}

	@Override
	public void update( long timestamp, Number... values )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void flush()
	{
		// TODO Write to the proper Track of the current Execution.

	}

	@Override
	public Map<String, Class<? extends Number>> getStatisticsNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Number> T getStatisticValue( String statisticName, String instance )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Number> Iterable<DataPoint<T>> getStatisticRange( String statisticName, String instance,
			long start, long end )
	{
		// TODO Auto-generated method stub
		return null;
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
			return new AverageStatisticWriter( statisticsManager, variable );
		}
	}
}