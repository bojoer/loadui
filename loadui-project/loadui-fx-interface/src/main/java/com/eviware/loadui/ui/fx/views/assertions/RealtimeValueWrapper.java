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
