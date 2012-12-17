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

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.property.Property;

/**
 * A holder of Properties.
 * 
 * @author dain.nilsson
 */
public interface PropertyHolder extends EventFirer
{
	/**
	 * Get the held Property with the given name.
	 * 
	 * @param propertyName
	 *           The name of the Property to get.
	 * @return The Property with the given name, or null if no such Property
	 *         exists.
	 */
	public Property<?> getProperty( String propertyName );

	/**
	 * Gets all the held Properties.
	 * 
	 * @return A Collection of all the Properties.
	 */
	@Nonnull
	public Collection<Property<?>> getProperties();

	/**
	 * Renames a Property.
	 * 
	 * @param oldName
	 *           The name of the Property to rename.
	 * @param newName
	 *           The new name to give the Property.
	 */
	public void renameProperty( String oldName, String newName );

	/**
	 * Creates a new Property of the given type if it does not already exist, or
	 * returns the already existing Property.
	 * 
	 * @param <T>
	 *           The type of the Property to create.
	 * @param propertyName
	 *           The name to give the new Property.
	 * @param propertyType
	 *           The type of the Property.
	 * @return The newly created Property.
	 */
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType );

	/**
	 * Creates a new Property of the given type if it does not already exist, or
	 * returns the already existing Property.
	 * 
	 * @param <T>
	 *           The type of the Property to create.
	 * @param propertyName
	 *           The name to give the new Property.
	 * @param propertyType
	 *           The type of the Property.
	 * @param initialValue
	 *           An initial value to use for the Property, if it does not already
	 *           exist.
	 * @return The newly created Property.
	 */
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue );

	/**
	 * Creates a new Property of the given type if it does not already exist, or
	 * returns the already existing Property.
	 * 
	 * @param <T>
	 *           The type of the Property to create.
	 * @param propertyName
	 *           The name to give the new Property.
	 * @param propertyType
	 *           The type of the Property.
	 * @param initialValue
	 *           An initial value to use for the Property, if it does not already
	 *           exist.
	 * @param propagates
	 *           If set to false, this Property will not propagate between
	 *           Controller and Agents.
	 * @return The newly created Property.
	 */
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue,
			boolean propagates );

	/**
	 * Deletes a Property.
	 * 
	 * @param propertyName
	 *           The name of the Property to delete.
	 */
	public void deleteProperty( String propertyName );
}
