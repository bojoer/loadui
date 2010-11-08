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

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsWriter;

public class StatisticImpl<T extends Number> implements Statistic<T>
{
	private final Class<T> type;
	private final StatisticsWriter writer;
	private final StatisticVariable variable;
	private final String name;
	private final String instance;

	public StatisticImpl( StatisticsWriter writer, StatisticVariable variable, String name, String instance,
			Class<T> type )
	{
		this.type = type;
		this.writer = writer;
		this.variable = variable;
		this.name = name;
		this.instance = instance;
	}

	@Override
	public Class<T> getType()
	{
		return type;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public T getValue()
	{
		return ( T )writer.getStatisticValue( name, instance );
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public StatisticVariable getStatisticVariable()
	{
		return variable;
	}

	@Override
	public Iterable<DataPoint<T>> getPeriod( long start, long end )
	{
		return writer.getStatisticRange( name, instance, start, end );
	}
}