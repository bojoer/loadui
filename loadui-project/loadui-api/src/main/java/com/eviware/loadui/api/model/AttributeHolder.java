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
package com.eviware.loadui.api.model;

import java.util.Collection;

import javax.annotation.Nonnull;

public interface AttributeHolder
{
	/**
	 * Sets a string attribute for the AttributeHolder, which will be saved when
	 * the AttributeHolder is saved, and loaded when the AttributeHolder is
	 * loaded.
	 * 
	 * @param key
	 *           The name of the attribute to set.
	 * @param value
	 *           The value to set.
	 */
	public void setAttribute( String key, String value );

	/**
	 * Gets a String attribute previously stored for the AttributeHolder.
	 * 
	 * @param key
	 *           The name of the attribute to get.
	 * @param defaultValue
	 *           A default String to return if the attribute does not exist.
	 * @return The value of the attribute, or the default value if the attribute
	 *         does not exist.
	 */
	public String getAttribute( String key, String defaultValue );

	/**
	 * Removes a String attribute previously stored for the AttributeHolder, if
	 * it exists.
	 * 
	 * @param key
	 *           The name of the attribute to remove.
	 */
	public void removeAttribute( String key );

	/**
	 * Gets a list of all attributes stored for the AttributeHolder.
	 * 
	 * @return a Collection<String> of all the attribute keys.
	 */
	@Nonnull
	public Collection<String> getAttributes();
}
