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
package com.eviware.loadui.util;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.traits.Initializable;

/**
 * Utility class for initializing Initializables.
 * 
 * @author dain.nilsson
 */
public class InitializableUtils
{
	/**
	 * Calls init(), followed by postInit(), followed by firing the INITIALIZED
	 * event for EventFirers.
	 * 
	 * @param object
	 * @return
	 */
	public static <T extends Initializable> T initialize( T object )
	{
		object.init();
		object.postInit();

		if( object instanceof EventFirer )
		{
			EventFirer eventFirer = ( EventFirer )object;
			eventFirer.fireEvent( new BaseEvent( eventFirer, Initializable.INITIALIZED ) );
		}

		return object;
	}
}
