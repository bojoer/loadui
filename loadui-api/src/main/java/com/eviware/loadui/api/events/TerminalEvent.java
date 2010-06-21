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
package com.eviware.loadui.api.events;

import com.eviware.loadui.api.terminal.OutputTerminal;

/**
 * An event signaling change to an OutputTerminal.
 * 
 * @author dain.nilsson
 */
public abstract class TerminalEvent extends BaseEvent
{
	private static final long serialVersionUID = -4106963302707285374L;

	private final OutputTerminal terminal;

	/**
	 * Constructs a TerminalEvent to be fired.
	 * 
	 * @param terminal
	 *           The OutputTerminal firing the event.
	 */
	public TerminalEvent( OutputTerminal terminal )
	{
		super( terminal.getTerminalHolder(), terminal.getId() );
		this.terminal = terminal;
	}

	/**
	 * Gets the OutputTerminal which is the source of the TerminalEvent.
	 * 
	 * @return The OutputTerminal firing the event.
	 */
	public OutputTerminal getOutputTerminal()
	{
		return terminal;
	}
}
