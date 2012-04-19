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
package com.eviware.loadui.api.component.categories;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;

public interface OutputCategory extends ComponentBehavior
{
	/**
	 * The String identifier of the category.
	 */
	public static final String CATEGORY = "Output";

	/**
	 * The color of the category.
	 */
	public static final String COLOR = "#0aa241";

	/**
	 * The label of the InputTerminal which is returned by getInputTerminal().
	 */
	public static final String INPUT_TERMINAL = "inputTerminal";

	/**
	 * The label of the OutputTerminal which is returned by getOutputTerminal().
	 */
	public static final String OUTPUT_TERMINAL = "outputTerminal";

	/**
	 * The InputTerminal which is used to to direct TerminalMessages into the
	 * Output Component.
	 * 
	 * @return
	 */
	public InputTerminal getInputTerminal();

	/**
	 * The OutputTerminal which is used to pass through incoming
	 * TerminalMessages.
	 * 
	 * @return
	 */
	public OutputTerminal getOutputTerminal();
}
