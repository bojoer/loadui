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

import java.util.EventObject;

/**
 * A basic event fired by any EventFirer.
 * 
 * @author dain.nilsson
 */
public class BaseEvent extends EventObject
{
	private static final long serialVersionUID = 930138360521728634L;
	private final String key;

	/**
	 * Constructs a BaseEvent to be fired.
	 * 
	 * @param source
	 *           The Object firing the event.
	 * @param key
	 *           The event key.
	 */
	public BaseEvent( EventFirer source, String key )
	{
		super( source );
		this.key = key;
	}

	/**
	 * Get the key of the event.
	 * 
	 * @return the key of the event.
	 */
	public String getKey()
	{
		return key;
	}

	@Override
	public EventFirer getSource()
	{
		return ( EventFirer )super.getSource();
	}
}
