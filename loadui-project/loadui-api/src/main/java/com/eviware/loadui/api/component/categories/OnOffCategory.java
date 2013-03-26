/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.api.component.categories;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.InputTerminal;

/**
 * OnOffCategory Components have a state Property, which can be turned on or off
 * to enable or disable the Component. This Property can also be controlled
 * using an InputTerminal.
 * 
 * @author dain.nilsson
 */
public interface OnOffCategory extends ComponentBehavior
{
	/**
	 * The label of the Property<Boolean> which is returned by
	 * getStateProperty().
	 */
	public static final String STATE_PROPERTY = "stateProperty";

	/**
	 * The name of the InputTerminal which is returned by getStateTerminal().
	 */
	public static final String STATE_TERMINAL = "stateTerminal";

	/**
	 * The name of the InputTerminal which is returned by getStateTerminal().
	 */
	public static final String STATE_TERMINAL_LABEL = "Component activation";

	/**
	 * The description of the InputTerminal.
	 */
	public static final String STATE_TERMINAL_DESCRIPTION = "Connect to a Scheduler to turn this component On or Off.";

	/**
	 * The key to be used to set the stateProperty for incoming messages.
	 */
	public static final String ENABLED_MESSAGE_PARAM = "Enabled";

	/**
	 * Returns the State Property which controls if the component is enabled or
	 * disabled.
	 * 
	 * @return
	 */
	public Property<Boolean> getStateProperty();

	/**
	 * Returns the state InputTerminal which can control the state of the
	 * component.
	 * 
	 * @return
	 */
	public InputTerminal getStateTerminal();
}
