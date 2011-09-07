/*
 * Copyright 2011 eviware software ab
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

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.eviware.loadui.api.layout.ActionLayoutComponent;
import com.eviware.loadui.util.MapUtils;
import com.google.common.collect.Sets;

public class ActionLayoutComponentImpl extends LayoutComponentImpl implements ActionLayoutComponent
{
	public final static String LABEL = "label";
	public final static String ACTION = "action";
	public final static String ASYNC = "async";

	private final Set<ActionEnabledListener> listeners = Sets
			.newSetFromMap( new WeakHashMap<ActionEnabledListener, Boolean>() );
	private boolean enabled = true;

	public ActionLayoutComponentImpl( Map<String, ?> args )
	{
		super( args );

		if( !( properties.get( ACTION ) instanceof Runnable ) )
			throw new IllegalArgumentException( "Illegal arguments: " + args );
	}

	@Override
	public Runnable getAction()
	{
		return ( Runnable )properties.get( ACTION );
	}

	@Override
	public String getLabel()
	{
		return MapUtils.getOr( properties, LABEL, "" );
	}

	@Override
	public boolean isAsynchronous()
	{
		return MapUtils.getOr( properties, ASYNC, true );
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled( boolean enabled )
	{
		if( this.enabled != enabled )
		{
			this.enabled = enabled;
			for( ActionEnabledListener listener : listeners )
				listener.stateChanged( this );
		}
	}

	@Override
	public void registerListener( ActionEnabledListener listener )
	{
		listeners.add( listener );
	}

	@Override
	public void unregisterListener( ActionEnabledListener listener )
	{
		listeners.remove( listener );
	}
}
