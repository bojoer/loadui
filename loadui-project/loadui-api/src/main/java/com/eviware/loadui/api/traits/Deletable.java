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
package com.eviware.loadui.api.traits;

/**
 * An Object which can be deleted.
 * 
 * @author dain.nilsson
 */
public interface Deletable
{
	/**
	 * If the Deletable also implements EventFirer, it should fire a BaseEvent
	 * with the DELETED constant as a key to inform listeners that the Deletable
	 * has been deleted.
	 */
	public final String DELETED = Deletable.class.getSimpleName() + "@deleted";

	/**
	 * Permanently removes the object (doesn't necessarily save the change to
	 * disk).
	 */
	public void delete();
}
