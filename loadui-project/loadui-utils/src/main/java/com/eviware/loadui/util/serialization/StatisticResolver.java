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
		statisticName = statistic.getName();
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
