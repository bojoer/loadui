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
package com.eviware.loadui.impl.layout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.eviware.loadui.api.layout.OptionsProvider;

public class OptionsProviderImpl<T> implements OptionsProvider<T>
{
	private final Set<OptionsListener> listeners = new HashSet<OptionsListener>();
	private final Map<Runnable, RunnableOptionsListener> runnableListeners = new HashMap<Runnable, RunnableOptionsListener>();
	private Iterable<T> options;
	private String nullString = "-";

	public void setNullString( String nullString )
	{
		this.nullString = nullString;
	}

	public OptionsProviderImpl( Iterable<T> iterable )
	{
		options = iterable;
	}

	public OptionsProviderImpl( T... options )
	{
		this.options = Arrays.asList( options );
	}

	@Override
	public String labelFor( T option )
	{
		return option == null ? nullString : option.toString();
	}

	@Override
	public void registerListener( Runnable onOptionsChange )
	{
		if( !runnableListeners.containsKey( onOptionsChange ) )
		{
			RunnableOptionsListener listener = new RunnableOptionsListener( onOptionsChange );
			runnableListeners.put( onOptionsChange, listener );
			registerListener( listener );
		}
	}

	@Override
	public void registerListener( OptionsListener listener )
	{
		listeners.add( listener );
	}

	public void setOptions( T... options )
	{
		this.options = Arrays.asList( options );
		for( OptionsListener listener : listeners )
			listener.onOptionsChange( this );
	}

	@Override
	public void unregisterListener( Runnable onOptionsChange )
	{
		unregisterListener( runnableListeners.remove( onOptionsChange ) );
	}

	@Override
	public void unregisterListener( OptionsListener listener )
	{
		listeners.remove( listener );
	}

	@Override
	public Iterator<T> iterator()
	{
		return options.iterator();
	}

}
