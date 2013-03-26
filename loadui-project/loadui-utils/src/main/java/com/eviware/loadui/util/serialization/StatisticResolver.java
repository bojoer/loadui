/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.serialization;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.util.BeanInjector;

@SuppressWarnings( "rawtypes" )
public class StatisticResolver implements Resolver<Statistic>
{
	private static final long serialVersionUID = 2811631530918631281L;

	private final String holderAddress;
	private final String variableName;
	private final String statisticName;
	private final String source;

	public StatisticResolver( Statistic statistic )
	{
		statisticName = statistic.getLabel();
		source = statistic.getSource();

		StatisticVariable variable = statistic.getStatisticVariable();
		variableName = variable.getLabel();

		StatisticHolder holder = variable.getStatisticHolder();
		holderAddress = holder.getId();
	}

	@Override
	public Class<Statistic> getType()
	{
		return Statistic.class;
	}

	@Override
	public Statistic<?> getValue()
	{
		StatisticHolder holder = ( StatisticHolder )BeanInjector.getBean( AddressableRegistry.class ).lookup(
				holderAddress );

		return holder.getStatisticVariable( variableName ).getStatistic( statisticName, source );
	}
}
