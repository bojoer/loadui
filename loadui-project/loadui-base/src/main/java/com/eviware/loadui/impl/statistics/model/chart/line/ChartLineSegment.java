/* 
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.impl.statistics.model.chart.line;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.util.StringUtils;
import com.google.common.base.Preconditions;

public class ChartLineSegment extends AbstractChartSegment implements LineSegment.Removable
{
	private final String variableName;
	private final String statisticName;
	private final String source;

	private Statistic<?> statistic;

	protected static final Logger log = LoggerFactory.getLogger( ChartLineSegment.class );

	public ChartLineSegment( ChartLineChartView chart, String variableName, String statisticName, String source )
	{
		super( chart, StringUtils.serialize( Arrays.asList( chart.getChart().getOwner().getId(), variableName,
				statisticName, source ) ) );

		Preconditions.checkArgument( chart.getChart().getOwner() instanceof StatisticHolder,
				"Owner is not a StatisticHolder!" );

		this.variableName = variableName;
		this.statisticName = statisticName;
		this.source = source;
	}

	@Override
	public StatisticHolder getStatisticHolder()
	{
		return ( StatisticHolder )getChart().getOwner();
	}

	@Override
	public String getSource()
	{
		return source;
	}

	@Override
	public String getVariableName()
	{
		return variableName;
	}

	@Override
	public String getStatisticName()
	{
		return statisticName;
	}

	@Override
	public Statistic<?> getStatistic()
	{
		if( statistic == null )
		{
			final StatisticVariable statisticVariable = getStatisticHolder().getStatisticVariable( variableName );
			if( statisticVariable != null )
			{
				statistic = statisticVariable.getStatistic( statisticName, source );
			}
		}

		return statistic;

	}

	@Override
	public void remove()
	{
		super.remove();
		if( getChartView().getSegments().size() == 0 )
		{
			getChart().delete();
		}
	}
}