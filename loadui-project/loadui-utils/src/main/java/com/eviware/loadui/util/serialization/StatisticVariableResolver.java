package com.eviware.loadui.util.serialization;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.util.BeanInjector;

public class StatisticVariableResolver implements Resolver<StatisticVariable>
{
	private static final long serialVersionUID = -6992531821931967920L;

	private final String holderAddress;
	private final String variableName;

	public StatisticVariableResolver( StatisticVariable statisticVariable )
	{
		holderAddress = statisticVariable.getStatisticHolder().getId();
		variableName = statisticVariable.getLabel();
	}

	@Override
	public Class<StatisticVariable> getType()
	{
		return StatisticVariable.class;
	}

	@Override
	public StatisticVariable getValue()
	{
		StatisticHolder holder = ( StatisticHolder )BeanInjector.getBean( AddressableRegistry.class ).lookup(
				holderAddress );

		return holder.getStatisticVariable( variableName );
	}
}
