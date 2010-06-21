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

import com.eviware.loadui.api.model.RunnerItem;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;

/**
 * Signals a message being fired from a remote OutputTerminal.
 * 
 * @author dain.nilsson
 */
public class TerminalRemoteMessageEvent extends TerminalMessageEvent
{
	private static final long serialVersionUID = -7549002891636662868L;

	private final RunnerItem runner;

	/**
	 * Constructs a TerminalRemoteMessageEvent to be fired.
	 * 
	 * @param output
	 *           The OutputTerminal sending the TerminalMessage.
	 * @param message
	 *           The message to send.
	 * @param runner
	 *           The remote runner from which the TerminalMessage was sent.
	 */
	public TerminalRemoteMessageEvent( OutputTerminal output, TerminalMessage message, RunnerItem runner )
	{
		super( output, message );
		this.runner = runner;
	}

	/**
	 * Returns the RunnerItem associated with the event.
	 * 
	 * @return The runner
	 */
	public RunnerItem getRunner()
	{
		return runner;
	}
}
