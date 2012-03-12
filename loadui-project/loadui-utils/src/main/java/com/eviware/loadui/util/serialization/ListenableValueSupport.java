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
package com.eviware.loadui.util.serialization;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import com.eviware.loadui.api.serialization.ListenableValue;
import com.google.common.collect.ImmutableSet;

/**
 * Support class for implementing the ListenableValue interface. Calls to
 * update() will only notify the listeners when the value changes. Weak
 * References are used for listeners.
 * 
 * @author dain.nilsson
 */
public class ListenableValueSupport<T>
{
	private final Set<ListenableValue.ValueListener<? super T>> listeners = Collections
			.newSetFromMap( new WeakHashMap<ListenableValue.ValueListener<? super T>, Boolean>() );

	private T lastValue = null;

	public void update( T newValue )
	{
		lastValue = newValue;

		for( ListenableValue.ValueListener<? super T> listener : ImmutableSet.copyOf( listeners ) )
		{
			listener.update( newValue );
		}
	}

	public void addListener( ListenableValue.ValueListener<? super T> listener )
	{
		listeners.add( listener );
	}

	public void removeListener( ListenableValue.ValueListener<? super T> listener )
	{
		listeners.remove( listener );
	}

	public int getListenerCount()
	{
		return listeners.size();
	}

	public T getLastValue()
	{
		return lastValue;
	}
}
