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

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An object which has human-readable text description.
 * 
 * @author henrik.olsson
 */
public interface Describable
{
	/**
	 * If the Describable also implements EventFirer, it should fire a BaseEvent
	 * with the DESCRIPTION constant as a key to inform listeners that the
	 * description has changed.
	 */
	public static final String DESCRIPTION = Describable.class.getSimpleName() + "@description";

	/**
	 * Gets the description of the object.
	 */
	public String getDescription();

	/**
	 * A Describable object which has an editable description.
	 * 
	 * @author henrik.olsson
	 */
	public interface Mutable extends Describable
	{
		/**
		 * Sets the description of the object.
		 * 
		 * @param description
		 */
		public void setDescription( @NonNull String description );
	}
}
