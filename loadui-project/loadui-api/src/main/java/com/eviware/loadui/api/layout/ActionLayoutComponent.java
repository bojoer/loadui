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
package com.eviware.loadui.api.layout;

import com.eviware.loadui.api.traits.Labeled;

/**
 * A LayoutComponent which holds an action to perform when the user activates
 * it. It is generally rendered as a button which can be clicked to run the
 * action.
 * 
 * @author dain.nilsson
 */
public interface ActionLayoutComponent extends LayoutComponent, Labeled
{
	/**
	 * The action to perform when the component is activated.
	 * 
	 * @return
	 */
	public Runnable getAction();

	/**
	 * Checks to see if the action is enabled.
	 * 
	 * @return
	 */
	public boolean isEnabled();

	/**
	 * Checks to see if the action should be performed asynchronously (in a
	 * background Thread).
	 * 
	 * @return
	 */
	public boolean isAsynchronous();

	/**
	 * Enables the action in the interface. This is the default mode.
	 * 
	 * @param enabled
	 */
	public void setEnabled( boolean enabled );

	/**
	 * Registers a listener for enabled state changes.
	 * 
	 * @param listener
	 */
	public void registerListener( ActionEnabledListener listener );

	/**
	 * Unregisters a listener for enabled state changes.
	 * 
	 * @param listener
	 */
	public void unregisterListener( ActionEnabledListener listener );

	/**
	 * A Listener for enabled state changes.
	 * 
	 * @author dain.nilsson
	 */
	public interface ActionEnabledListener
	{
		/**
		 * Called when the enabled state changes.
		 * 
		 * @param source
		 */
		public void stateChanged( ActionLayoutComponent source );
	}
}
