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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsWriter;
import com.eviware.loadui.util.CacheMap;

/**
 * Implementation of a StatisticVariable.
 * 
 * @author dain.nilsson
 */
public class StatisticVariableImpl implements StatisticVariable
{
	private final String name;
	private final StatisticHolder parent;
	private final Set<StatisticsWriter> writers = new HashSet<StatisticsWriter>();
	private final Set<String> sources = new HashSet<String>();
	private final Set<String> statisticNames = new HashSet<String>();
	private final CacheMap<String, StatisticImpl<?>> statisticCache = new CacheMap<String, StatisticImpl<?>>();

	public StatisticVariableImpl( StatisticHolder parent, String name )
	{
		this.name = name;
		this.parent = parent;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public StatisticHolder getStatisticHolder()
	{
		return parent;
	}

	public void addStatisticsWriter( StatisticsWriter writer )
	{
		if( writers.add( writer ) )
			statisticNames.addAll( writer.getStatisticsNames().keySet() );
	}

	@Override
	public Collection<String> getSources()
	{
		return Collections.unmodifiableSet( sources );
	}

	@Override
	public Collection<String> getStatisticNames()
	{
		return Collections.unmodifiableSet( statisticNames );
	}

	@Override
	public Statistic<?> getStatistic( final String statisticName, final String source )
	{
		return statisticCache.getOrCreate( statisticName.length() + ":" + statisticName + source,
				new Callable<StatisticImpl<?>>()
				{
					@Override
					@SuppressWarnings( { "unchecked", "rawtypes" } )
					public StatisticImpl<?> call() throws Exception
					{
						for( StatisticsWriter writer : writers )
							for( Entry<String, Class<? extends Number>> entry : writer.getStatisticsNames().entrySet() )
								if( statisticName.equals( entry.getKey() ) )
									return new StatisticImpl( writer, StatisticVariableImpl.this, statisticName, source, entry
											.getValue() );
						throw new NullPointerException();
					}
				} );
	}
}
