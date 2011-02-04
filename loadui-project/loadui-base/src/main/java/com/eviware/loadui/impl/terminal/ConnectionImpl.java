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

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.TerminalConnectionEvent;
import com.eviware.loadui.api.events.TerminalEvent;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.config.ConnectionConfig;
import com.eviware.loadui.util.BeanInjector;

public class ConnectionImpl extends ConnectionBase
{
	private final Listener listener = new Listener();
	private final ConnectionConfig config;

	// private final CanvasItem canvas;

	public ConnectionImpl( ConnectionConfig config, OutputTerminal output, InputTerminal input )
	{
		super( output, input );
		config.setOutputTerminalId( output.getId() );
		config.setInputTerminalId( input.getId() );

		this.config = config;
		// canvas = output.getComponent().getCanvas();
		// if( canvas != input.getComponent().getCanvas() )
		// throw new IllegalArgumentException(
		// "ConnectionImpl requires both Terminals to be in the same Canvas." );
		output.addEventListener( TerminalEvent.class, listener );
		fireTerminalConnectionEvent( TerminalConnectionEvent.Event.CONNECT );
	}

	public ConnectionImpl( ConnectionConfig config )
	{
		super(
				( OutputTerminal )BeanInjector.getBean( AddressableRegistry.class ).lookup( config.getOutputTerminalId() ),
				( InputTerminal )BeanInjector.getBean( AddressableRegistry.class ).lookup( config.getInputTerminalId() ) );

		this.config = config;
		// canvas = getOutputTerminal().getComponent().getCanvas();
		// if( canvas != getInputTerminal().getComponent().getCanvas() )
		// throw new IllegalArgumentException(
		// "ConnectionImpl requires both Terminals to be in the same Canvas." );
		getOutputTerminal().addEventListener( TerminalEvent.class, listener );
		fireTerminalConnectionEvent( TerminalConnectionEvent.Event.CONNECT );
	}

	public ConnectionConfig getConfig()
	{
		return config;
	}

	@Override
	public void disconnect()
	{
		fireTerminalConnectionEvent( TerminalConnectionEvent.Event.DISCONNECT );
		getOutputTerminal().removeEventListener( TerminalEvent.class, listener );
	}

	private void fireTerminalConnectionEvent( TerminalConnectionEvent.Event event )
	{
		OutputTerminal output = getOutputTerminal();
		if( output instanceof OutputTerminalImpl )
			( ( OutputTerminalImpl )output ).fireEvent( new TerminalConnectionEvent( this, output, getInputTerminal(),
					event ) );
	}

	protected void handleTerminalEvent( TerminalEvent event )
	{
		InputTerminal inputTerminal = getInputTerminal();
		if( event instanceof TerminalConnectionEvent )
		{
			if( ( ( TerminalConnectionEvent )event ).getConnection() == this )
			{
				inputTerminal.getTerminalHolder().handleTerminalEvent( inputTerminal, event );
				getOutputTerminal().getTerminalHolder().handleTerminalEvent( inputTerminal, event );
			}
		}
		else
		{
			inputTerminal.getTerminalHolder().handleTerminalEvent( inputTerminal, event );
		}
	}

	private class Listener implements EventHandler<TerminalEvent>
	{
		@Override
		public void handleEvent( TerminalEvent event )
		{
			handleTerminalEvent( event );
		}
	}
}
