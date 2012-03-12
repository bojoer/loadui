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

import javax.annotation.Nonnull;

/**
 * An object which has a human readable label which should be used when
 * displaying the object to a user.
 * 
 * @author dain.nilsson
 */
public interface Labeled
{
	/**
	 * If the Labeled also implements EventFirer, it should fire a BaseEvent with
	 * the LABEL constant as a key to inform listeners that the label has
	 * changed.
	 */
	public static final String LABEL = Labeled.class.getSimpleName() + "@label";

	/**
	 * Gets the label of the object.
	 */
	public String getLabel();

	/**
	 * A Labeled object which has an editable label.
	 * 
	 * @author dain.nilsson
	 */
	public interface Mutable extends Labeled
	{
		/**
		 * Sets the label of the object.
		 * 
		 * @param label
		 */
		public void setLabel( @Nonnull String label );
	}
}