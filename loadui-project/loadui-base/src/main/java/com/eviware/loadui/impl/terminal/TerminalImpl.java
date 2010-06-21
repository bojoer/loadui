/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.terminal;

import java.util.EventObject;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.addressable.AddressableRegistry.DuplicateAddressException;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.TerminalHolder;
import com.eviware.loadui.impl.events.EventSupport;
import com.eviware.loadui.util.BeanInjector;

public abstract class TerminalImpl implements Terminal
{
	private final EventSupport eventsupport = new EventSupport();
	private final TerminalHolder owner;
	private final String label;
	private final String description;

	public TerminalImpl( TerminalHolder component, String label, String description )
	{
		this.owner = component;
		this.label = label;
		this.description = description;

		try
		{
			BeanInjector.getBean( AddressableRegistry.class ).register( this );
		}
		catch( DuplicateAddressException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public String getId()
	{
		return owner.getId() + "/" + label;
	}

	@Override
	public TerminalHolder getTerminalHolder()
	{
		return owner;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventsupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventsupport.removeEventListener( type, listener );
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventsupport.fireEvent( event );
	}

	@Override
	public void clearEventListeners()
	{
		eventsupport.clearEventListeners();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[label=" + getLabel() + "]";
	}
}
