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
 * An object which needs to be released when no longer needed, in order to
 * release its resources.
 * 
 * @author dain.nilsson
 */
public interface Releasable
{
	/**
	 * If the Releasable also implements EventFirer, it should fire a BaseEvent
	 * with the RELEASED constant as a key to inform listeners that it has been
	 * released.
	 */
	public static final String RELEASED = Releasable.class.getSimpleName() + "@released";

	/**
	 * Causes the Releasable to release its resources and stop anything that it
	 * is doing. After calling this, other methods on the Releasable may no
	 * longer work as intended.
	 */
	public void release();
}
