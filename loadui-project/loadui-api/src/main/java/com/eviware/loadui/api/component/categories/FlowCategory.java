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
package com.eviware.loadui.api.component.categories;

import java.util.List;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;

/**
 * Flow components direct the flow of TerminalMessages between components and
 * help define the behavior.
 * 
 * @author dain.nilsson
 */
public interface FlowCategory extends ComponentBehavior
{
	/**
	 * The String identifier of the category.
	 */
	public static final String CATEGORY = "Flow";

	/**
	 * The color of the category.
	 */
	public static final String COLOR = "#a900ce";

	/**
	 * The label of the InputTerminal which is returned by getIncomingTerminal().
	 */
	public static final String INCOMING_TERMINAL = "incomingTerminal";

	/**
	 * The base of the label of the OutputTerminals which are returned by
	 * getOutgoingTerminalList(). Besides this base, each OutputTerminal should
	 * have a space and a number appended, starting with 1.
	 * 
	 * For example: "outgoingTerminal 1", "outgoingTerminal 2", etc.
	 */
	public static final String OUTGOING_TERMINAL = "outgoingTerminal";

	/**
	 * Gets the InputTerminal of the component.
	 * 
	 * @return
	 */
	public InputTerminal getIncomingTerminal();

	/**
	 * Gets a List of the OutputTerminals of the Component, ordered according to
	 * the numbering of their labels.
	 * 
	 * @return
	 */
	public List<OutputTerminal> getOutgoingTerminalList();
}
