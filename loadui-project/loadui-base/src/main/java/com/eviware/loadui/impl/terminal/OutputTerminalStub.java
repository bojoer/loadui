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
package com.eviware.loadui.impl.terminal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.Map;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.util.events.EventSupport;
import com.google.common.base.Objects;

public class OutputTerminalStub implements OutputTerminal
{
	private final String id;
	private String label;
	private Map<String, Class<?>> signature = Collections.emptyMap();
	private final EventSupport eventsupport = new EventSupport();

	public OutputTerminalStub( String id, String label )
	{
		this.id = id;
		this.label = label;
	}

	public void setMessageSignature( Map<String, Class<?>> signature )
	{
		this.signature = Collections.unmodifiableMap( signature );
	}

	@Override
	public Connection connectTo( InputTerminal input )
	{
		throw new UnsupportedOperationException( "Not implemented" );
	}

	@Override
	public Map<String, Class<?>> getMessageSignature()
	{
		return signature;
	}

	@Override
	public ComponentItem getTerminalHolder()
	{
		// throw new UnsupportedOperationException( "Not implemented" );
		return ( ComponentItem )Proxy.newProxyInstance( ComponentItem.class.getClassLoader(),
				new Class<?>[] { ComponentItem.class }, new InvocationHandler()
				{
					@Override
					public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
					{
						return null;
					}
				} );
	}

	@Override
	public Collection<Connection> getConnections()
	{
		throw new UnsupportedOperationException( "Not implemented" );
	}

	@Override
	public String getDescription()
	{
		return "";
	}

	@Override
	public void setDescription( String description )
	{
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
	public String getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return label;
	}
}
