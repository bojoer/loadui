package com.eviware.loadui.impl.statistics;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.util.serialization.ListenableValueSupport;

public class ListenableStatisticVariableImpl extends StatisticVariableImpl implements ListenableValue<Number>
{
	private final ListenableValueSupport<Number> listenableValueSupport = new ListenableValueSupport<Number>();

	public ListenableStatisticVariableImpl( ExecutionManager executionManager, StatisticHolder parent, String name,
			AddressableRegistry addressableRegistry, String description )
	{
		super( executionManager, parent, name, addressableRegistry, description );
	}

	@Override
	public void update( long timestamp, Number value )
	{
		super.update( timestamp, value );

		listenableValueSupport.update( value );
	}

	@Override
	public Class<Number> getType()
	{
		return Number.class;
	}

	@Override
	public Number getValue()
	{
		return listenableValueSupport.getLastValue();
	}

	@Override
	public void addListener( ListenableValue.ValueListener<? super Number> listener )
	{
		listenableValueSupport.addListener( listener );
	}

	@Override
	public void removeListener( ValueListener<? super Number> listener )
	{
		listenableValueSupport.removeListener( listener );
	}
}
