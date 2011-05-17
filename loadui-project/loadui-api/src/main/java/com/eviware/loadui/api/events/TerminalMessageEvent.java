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

import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;

/**
 * Signals a message being fired from an OutputTerminal.
 * 
 * @author dain.nilsson
 */
public class TerminalMessageEvent extends TerminalEvent
{
	private static final long serialVersionUID = -1630327784848582328L;

	private final TerminalMessage message;

	/**
	 * Constructs a TerminalMessageEvent to be fired.
	 * 
	 * @param output
	 *           The OutputTerminal sending the TerminalMessage.
	 * @param message
	 *           The message to send.
	 */
	public TerminalMessageEvent( OutputTerminal output, TerminalMessage message )
	{
		super( output );
		this.message = message;
	}

	/**
	 * The TerminalMessage being sent.
	 * 
	 * @return The message being sent.
	 */
	public TerminalMessage getMessage()
	{
		return message;
	}
}
