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

import java.util.Collections;
import java.util.Map;

import com.eviware.loadui.api.terminal.OutputTerminal;

/**
 * Signals that an OutputTerminal has changed its signature.
 * 
 * @author dain.nilsson
 */
public class TerminalSignatureEvent extends TerminalEvent
{
	private static final long serialVersionUID = -4119904939041026948L;

	private final Map<String, Class<?>> signature;

	/**
	 * Constructs a TerminalSignatureEvent to be fired.
	 * 
	 * @param output
	 *           The OutputTerminal which is changing its signature.
	 * @param signature
	 *           The new signature.
	 */
	public TerminalSignatureEvent( OutputTerminal output, Map<String, Class<?>> signature )
	{
		super( output );
		this.signature = Collections.unmodifiableMap( signature );
	}

	/**
	 * Gets the signature associated with this event.
	 * 
	 * @return The signature
	 */
	public Map<String, Class<?>> getSignature()
	{
		return signature;
	}
}
