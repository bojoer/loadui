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
package com.eviware.loadui.impl.statistics;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.util.serialization.ListenableValueSupport;

public class ListenableStatisticVariableImpl extends StatisticVariableImpl implements ListenableValue<Number>
{
	private final ListenableValueSupport<Number> listenableValueSupport = new ListenableValueSupport<>();

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
