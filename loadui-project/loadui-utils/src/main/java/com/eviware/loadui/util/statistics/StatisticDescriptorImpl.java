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
}
