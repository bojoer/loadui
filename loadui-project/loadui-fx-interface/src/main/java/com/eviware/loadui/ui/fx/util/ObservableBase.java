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
package com.eviware.loadui.ui.fx.util;

import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import com.google.common.collect.Sets;

public class ObservableBase implements Observable
{
	private final Set<InvalidationListener> listeners = Sets.newCopyOnWriteArraySet();

	@Override
	public void addListener( InvalidationListener listener )
	{
		listeners.add( listener );
	}

	@Override
	public void removeListener( InvalidationListener listener )
	{
		listeners.remove( listener );
	}

	protected void fireInvalidation()
	{
		for( InvalidationListener listener : listeners )
		{
			listener.invalidated( this );
		}
	}
}
