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
import java.util.Set;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsWriter;

/**
 * Implementation of a StatisticVariable.
 * 
 * @author dain.nilsson
 */
public class StatisticVariableImpl implements StatisticVariable
{
	private final Set<StatisticsWriter> writers = new HashSet<StatisticsWriter>();
	private final Set<String> instances = new HashSet<String>();
	private final Set<String> statisticNames = new HashSet<String>();

	public StatisticVariableImpl( StatisticHolder parent )
	{

	}

	public void addStatisticsWriter( StatisticsWriter writer )
	{
		if( writers.add( writer ) )
			for( String statisticName : writer.getStatisticsNames().keySet() )
				statisticNames.add( statisticName );
	}

	@Override
	public Collection<String> getInstances()
	{
		return Collections.unmodifiableSet( instances );
	}

	@Override
	public Collection<String> getStatisticNames()
	{
		return Collections.unmodifiableSet( statisticNames );
	}

	@Override
	public Statistic<?> getStatistic( String statisticName, String instance )
	{
		for( StatisticsWriter writer : writers )
			if( writer.getStatisticsNames().keySet().contains( statisticName ) )
				return null;
		// TODO
		return null;
	}
}
