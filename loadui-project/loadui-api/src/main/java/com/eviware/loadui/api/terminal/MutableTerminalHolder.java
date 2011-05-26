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
package com.eviware.loadui.api.terminal;

public interface MutableTerminalHolder extends TerminalHolder
{
	/**
	 * Creates and returns a new InputTerminal using the given label and
	 * description. If an InputTerminal already exists with the given label, that
	 * Terminal is instead returned, and no new Terminal is created.
	 * 
	 * @param name
	 *           The name to give to the new OutputTerminal, must be unique per
	 *           TerminalHolder.
	 * @param label
	 *           The label to give the new InputTerminal.
	 * @param description
	 *           A description of the Terminal.
	 * @return An InputTerminal with the given label.
	 */
	public InputTerminal createInput( String name, String label, String description );

	/**
	 * Creates and returns a new OutputTerminal using the given label and
	 * description. If an OutputTerminal already exists with the given label,
	 * that Terminal is instead returned, and no new Terminal is created.
	 * 
	 * @param name
	 *           The name to give to the new OutputTerminal, must be unique per
	 *           TerminalHolder.
	 * @param label
	 *           The label to give the new OutputTerminal.
	 * @param description
	 *           A description of the Terminal.
	 * @return An OutputTerminal with the given label.
	 */
	public OutputTerminal createOutput( String name, String label, String description );

	/**
	 * Removes a Terminal from the Component.
	 * 
	 * @param terminal
	 *           The Terminal to remove.
	 */
	public void deleteTerminal( Terminal terminal );
}
