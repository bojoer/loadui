package com.eviware.loadui.ui.fx.views.assertions;

import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.util.serialization.StatisticVariableResolver;

public class RealtimeValueWrapper<T extends StatisticVariable & ListenableValue<Number>> implements
		AssertableWrapper<T>
{
	private final T listenableValue;

	public RealtimeValueWrapper( T listenableValue )
	{
		this.listenableValue = listenableValue;
	}

	@Override
	public String getLabel()
	{
		return "Real-time value";
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Resolver getResolver()
	{
		return new StatisticVariableResolver( listenableValue );
	}

	@Override
	public T getAssertable()
	{
		return listenableValue;
	}
}
