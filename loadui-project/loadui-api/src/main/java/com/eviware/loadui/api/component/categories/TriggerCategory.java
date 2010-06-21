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
package com.eviware.loadui.api.component.categories;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;

/**
 * Trigger components drive other components by periodically outputting trigger
 * messages through a designated OutputTerminal. They also have a state, which
 * can be either enabled (true) or disabled (false). When the component is
 * disabled, no trigger messages will be sent.
 * 
 * @author dain.nilsson
 */
public interface TriggerCategory extends ComponentBehavior
{
	/**
	 * The String identifier of the category.
	 */
	public static final String CATEGORY = "Generators";

	/**
	 * The color of the category.
	 */
	public static final String COLOR = "#ec420b";

	/**
	 * The label of the Property<Boolean> which is returned by
	 * getStateProperty().
	 */
	public static final String STATE_PROPERTY = "stateProperty";

	/**
	 * The label of the InputTerminal which is returned by getStateTerminal().
	 */
	public static final String STATE_TERMINAL = "stateTerminal";

	/**
	 * The label of the OutputTerminal which is returned by getTriggerTerminal().
	 */
	public static final String TRIGGER_TERMINAL = "triggerTerminal";

	/**
	 * The key to be used to set the stateProperty for incomming messages.
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
	 * Returns the OutputTerminal which outputs the trigger message.
	 * 
	 * @return
	 */
	public OutputTerminal getTriggerTerminal();

	/**
	 * Returns the state InputTerminal which can control the state of the
	 * component.
	 * 
	 * @return
	 */
	public InputTerminal getStateTerminal();
}
