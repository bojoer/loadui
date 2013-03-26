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
package com.eviware.loadui.util.statistics;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;

public class StatisticDescriptorImpl implements Statistic.Descriptor
{
	private static final long serialVersionUID = 1L;

	private final StatisticHolder statisticHolder;
	private final String variableLabel;
	private final String statisticLabel;
	private final String source;

	public StatisticDescriptorImpl( Statistic<?> statistic )
	{
		this( statistic.getStatisticVariable().getStatisticHolder(), statistic.getStatisticVariable().getLabel(),
				statistic.getLabel(), statistic.getSource() );
	}

	public StatisticDescriptorImpl( StatisticHolder statisticHolder, String variableLabel, String statisticLabel,
			String source )
	{
		this.statisticHolder = statisticHolder;
		this.variableLabel = variableLabel;
		this.statisticLabel = statisticLabel;
		this.source = source;
	}

	@Override
	@SuppressWarnings( "rawtypes" )
	public Class<Statistic> getType()
	{
		return Statistic.class;
	}

	@Override
	public Statistic<?> getValue()
	{
		return statisticHolder.getStatisticVariable( variableLabel ).getStatistic( statisticLabel, source );
	}

	@Override
	public String getStatisticLabel()
	{
		return statisticLabel;
	}

	@Override
	public String getStatisticVariableLabel()
	{
		return variableLabel;
	}

	@Override
	public String getSource()
	{
		return source;
	}

	@Override
	public String toString()
	{
		return "StatisticDescriptor[holder: " + statisticHolder + ", variable: " + variableLabel + ", statistic: "
				+ statisticLabel + ", source: " + source + "]";
	}
}
