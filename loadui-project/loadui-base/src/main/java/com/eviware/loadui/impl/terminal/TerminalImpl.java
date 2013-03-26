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
package com.eviware.loadui.impl.terminal;

import java.util.EventObject;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.addressable.AddressableRegistry.DuplicateAddressException;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.TerminalHolder;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public abstract class TerminalImpl implements Terminal, Releasable
{
	private final EventSupport eventSupport = new EventSupport( this );
	private final TerminalHolder owner;
	private final String name;
	private String label;
	private String description;

	public TerminalImpl( TerminalHolder component, String name, String label, String description )
	{
		this.owner = Preconditions.checkNotNull( component );
		this.name = Preconditions.checkNotNull( name );
		this.label = Preconditions.checkNotNull( label );
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
		return owner.getId() + "/" + name;
	}

	@Override
	public String getName()
	{
		return name;
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
	public void setLabel( String label )
	{
		if( !Objects.equal( this.label, label ) )
		{
			this.label = label;
			fireEvent( new BaseEvent( this, LABEL ) );
		}
	}

	@Override
	public void setDescription( String description )
	{
		this.description = description;
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void release()
	{
		BeanInjector.getBean( AddressableRegistry.class ).unregister( this );
		ReleasableUtils.release( eventSupport );
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[label=" + getLabel() + "]";
	}
}
