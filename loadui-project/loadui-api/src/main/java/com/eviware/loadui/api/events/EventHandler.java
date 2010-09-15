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

import java.util.EventListener;
import java.util.EventObject;

/**
 * A handler for Events which can be used to listen for events of a particular
 * type fired from an EventFirer.
 * 
 * @author dain.nilsson
 * 
 * @param <T>
 *           The type of event to listen for.
 */
public interface EventHandler<T extends EventObject> extends EventListener
{
	/**
	 * Invoked when an event is fired in an EventFirer that this EventHandler is
	 * listening to.
	 * 
	 * @param event
	 *           The event fired.
	 */
	public void handleEvent( T event );
}
