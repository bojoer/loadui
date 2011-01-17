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
package com.eviware.loadui.api.terminal;

import java.util.Collection;

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.TerminalEvent;
import com.eviware.loadui.api.model.CanvasItem;

public interface TerminalHolder extends EventFirer, Addressable
{
	public static final String TERMINALS = TerminalHolder.class.getName() + "@terminals";

	/**
	 * Get the Terminals that belong to this TerminalHolder.
	 * 
	 * @return A Collection of Terminals.
	 */
	public Collection<Terminal> getTerminals();

	/**
	 * Gets a specific Terminal by its label, or null if no such Terminal exists.
	 * 
	 * @param label
	 *           The label of the desired Terminal.
	 * @return The Terminal with the given label, or null.
	 */
	public Terminal getTerminalByLabel( String label );

	/**
	 * Causes the TerminalHolder to react to a TerminalEvent.
	 * 
	 * @param input
	 *           The InputTerminal which is the target for this event.
	 * @param event
	 *           The TerminalEvent to react to.
	 */
	public void handleTerminalEvent( InputTerminal input, TerminalEvent event );

	/**
	 * Get the canvas which holds this CanvasObjectItem.
	 * 
	 * @return The parent CanvasItem.
	 */
	public CanvasItem getCanvas();
}