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
package com.eviware.loadui.impl.terminal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.CollectionEvent.Event;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.api.terminal.TerminalHolder;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.ReleasableUtils;

public class TerminalHolderSupport implements Releasable
{
	private final TerminalHolder owner;
	private final Map<String, Terminal> terminals = new LinkedHashMap<>();

	public TerminalHolderSupport( TerminalHolder owner )
	{
		this.owner = owner;
	}

	public InputTerminalImpl createInput( String name, String label, String description )
	{
		if( terminals.containsKey( name ) )
		{
			Terminal existing = terminals.get( name );
			if( existing instanceof InputTerminalImpl )
				return ( InputTerminalImpl )existing;
			throw new IllegalArgumentException( "TerminalHolder already has a Terminal with label '" + label
					+ "' which is of different type than the requested." );
		}

		InputTerminalImpl terminal = new InputTerminalImpl( owner, name, label, description );
		terminals.put( name, terminal );
		owner.fireEvent( new CollectionEvent( owner, TerminalHolder.TERMINALS, Event.ADDED, terminal ) );

		return terminal;
	}

	public OutputTerminalImpl createOutput( String name, String label, String description )
	{
		if( terminals.containsKey( name ) )
		{
			Terminal existing = terminals.get( name );
			if( existing instanceof OutputTerminalImpl )
				return ( OutputTerminalImpl )existing;
			throw new IllegalArgumentException( "TerminalHolder already has a Terminal with label '" + label
					+ "' which is of different type than the requested." );
		}

		OutputTerminalImpl terminal = new OutputTerminalImpl( owner, name, label, description );
		terminals.put( name, terminal );
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
			ReleasableUtils.release( terminal );
		}
	}

	public Collection<Terminal> getTerminals()
	{
		return Collections.unmodifiableCollection( terminals.values() );
	}

	public Terminal getTerminalByName( String name )
	{
		return terminals.get( name );
	}

	public boolean containsTerminal( Terminal terminal )
	{
		return terminals.get( terminal.getName() ) == terminal;
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( terminals.values() );
		terminals.clear();
	}
}
