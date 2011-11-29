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
