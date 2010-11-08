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

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsWriter;

public abstract class AbstractStatisticsWriter implements StatisticsWriter
{
	private final StatisticVariable variable;

	public AbstractStatisticsWriter( StatisticVariable variable )
	{
		this.variable = variable;
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
}