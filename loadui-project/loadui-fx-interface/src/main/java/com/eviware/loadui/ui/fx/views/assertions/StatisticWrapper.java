package com.eviware.loadui.ui.fx.views.assertions;

import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.util.serialization.StatisticResolver;

public class StatisticWrapper<T extends Number> implements AssertableWrapper<Statistic<Number>>
{
	private final Statistic<Number> statistic;

	public StatisticWrapper( Statistic<Number> statistic )
	{
		this.statistic = statistic;
	}

	@Override
	public String getLabel()
	{
		return statistic.getLabel();
	}

	@Override
	public Resolver getResolver()
	{
		return new StatisticResolver( statistic );
	}

	@Override
	public Statistic<Number> getAssertable()
	{
		return statistic;
	}
}
