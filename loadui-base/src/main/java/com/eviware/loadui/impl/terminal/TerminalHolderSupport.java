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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.CollectionEvent.Event;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.TerminalHolder;
import com.eviware.loadui.util.BeanInjector;

public class TerminalHolderSupport
{
	private final TerminalHolder owner;
	private final Map<String, Terminal> terminals = new LinkedHashMap<String, Terminal>();
	private final AddressableRegistry addressableRegistry;

	public TerminalHolderSupport( TerminalHolder owner )
	{
		this.owner = owner;
		addressableRegistry = BeanInjector.getBean( AddressableRegistry.class );
	}

	public InputTerminal createInput( String label, String description )
	{
		if( terminals.containsKey( label ) )
		{
			Terminal existing = terminals.get( label );
			if( existing instanceof InputTerminal )
				return ( InputTerminal )existing;
			throw new IllegalArgumentException( "TerminalHolder already has a Terminal with label '" + label
					+ "' which is of different type than the requested." );
		}

		InputTerminal terminal = new InputTerminalImpl( owner, label, description );
		terminals.put( label, terminal );
		owner.fireEvent( new CollectionEvent( owner, TerminalHolder.TERMINALS, Event.ADDED, terminal ) );

		return terminal;
	}

	public OutputTerminal createOutput( String label, String description )
	{
		if( terminals.containsKey( label ) )
		{
			Terminal existing = terminals.get( label );
			if( existing instanceof InputTerminal )
				return ( OutputTerminal )existing;
			throw new IllegalArgumentException( "TerminalHolder already has a Terminal with label '" + label
					+ "' which is of different type than the requested." );
		}

		OutputTerminal terminal = new OutputTerminalImpl( owner, label, description );
		terminals.put( label, terminal );
		owner.fireEvent( new CollectionEvent( owner, TerminalHolder.TERMINALS, Event.ADDED, terminal ) );

		return terminal;
	}

	public void deleteTerminal( Terminal terminal )
	{
		if( terminals.values().remove( terminal ) )
		{
			for( Connection connection : owner.getCanvas().getConnections() )
				if( connection.getInputTerminal() == terminal || connection.getOutputTerminal() == terminal )
					connection.disconnect();

			owner.fireEvent( new CollectionEvent( owner, TerminalHolder.TERMINALS, Event.REMOVED, terminal ) );
			addressableRegistry.unregister( terminal );
		}
	}

	public Collection<Terminal> getTerminals()
	{
		return Collections.unmodifiableCollection( terminals.values() );
	}

	public boolean containsTerminal( Terminal terminal )
	{
		return terminals.get( terminal.getLabel() ) == terminal;
	}

	public void release()
	{
		for( Terminal terminal : terminals.values() )
			addressableRegistry.unregister( terminal );
		terminals.clear();
	}
}
