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
package com.eviware.loadui.api.events;

import java.util.EventObject;

/**
 * An object that fires events and thus can have listeners added and removed.
 * 
 * @author dain.nilsson
 */
public interface EventFirer
{
	/**
	 * Adds a listener to be notified whenever the EventFirer fires an event of
	 * the type T (or a subtype thereof).
	 * 
	 * @param <T>
	 *           The Type of event to listen for.
	 * @param type
	 *           The class literal for type T.
	 * @param listener
	 *           The EventHandler to invoke.
	 */
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener );

	/**
	 * Removes a listener from the EventFirer.
	 * 
	 * @param <T>
	 *           The Type of event that the listener was listening for.
	 * @param type
	 *           The class literal for type T.
	 * @param listener
	 *           The listener to remove.
	 */
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener );

	/**
	 * Clears the listeners added to the EventFirer.
	 */
	public void clearEventListeners();

	/**
	 * Fires a new EventObject for the EventFirer.
	 * 
	 * @param event
	 */
	public void fireEvent( EventObject event );
}
