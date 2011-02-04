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
package com.eviware.loadui.api.events;

/**
 * An Event which represents an Action which has been fired for a specific
 * ModelItem.
 * 
 * @author dain.nilsson
 */
public class ActionEvent extends BaseEvent
{
	private static final long serialVersionUID = 3473705224577013888L;

	/**
	 * Constructs an ActionEvent to be fired.
	 * 
	 * @param source
	 *           The source ModelItem upon which the ActionEvent was initially
	 *           fired.
	 * @param key
	 *           The name of the action.
	 */
	public ActionEvent( EventFirer source, String key )
	{
		super( source, key );
	}
}
