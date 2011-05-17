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
package com.eviware.loadui.api.events;

import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;

/**
 * Signals that a Connection has been either made or severed between an
 * OutputTerminal and an InputTerminal.
 * 
 * @author dain.nilsson
 */
public class TerminalConnectionEvent extends TerminalEvent
{
	private static final long serialVersionUID = -1064771252230666718L;

	private final Connection connection;

	/**
	 * The type of the event, either connected or disconnected.
	 */
	public static enum Event
	{
		CONNECT, DISCONNECT
	};

	private final InputTerminal input;
	private final Event event;

	/**
	 * Constructs a TerminalConnectionEvent to be fired.
	 * 
	 * @param connection
	 *           The Connection which is being changed.
	 * @param output
	 *           The OutputTerminal.
	 * @param input
	 *           The InputTerminal.
	 * @param event
	 *           The type of event occurring.
	 */
	public TerminalConnectionEvent( Connection connection, OutputTerminal output, InputTerminal input, Event event )
	{
		super( output );
		this.connection = connection;
		this.input = input;
		this.event = event;
	}

	/**
	 * Gets the InputTerminal part of the event.
	 * 
	 * @return The InputTerminal
	 */
	public InputTerminal getInputTerminal()
	{
		return input;
	}

	/**
	 * The type of event occurring to the Connection.
	 * 
	 * @return The event type.
	 */
	public Event getEvent()
	{
		return event;
	}

	/**
	 * The Connection which is changing.
	 * 
	 * @return The Connection
	 */
	public Connection getConnection()
	{
		return connection;
	}
}
