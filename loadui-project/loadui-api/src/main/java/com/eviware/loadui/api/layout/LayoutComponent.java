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
package com.eviware.loadui.api.layout;

/**
 * A visual component with layout information.
 * 
 * @author dain.nilsson
 */
public interface LayoutComponent
{
	/**
	 * A String holding layout information on how to render the LayoutComponent.
	 * The form for the String is the same as the Component Constrains for
	 * MigLayout (http://www.miglayout.com/).
	 * 
	 * @return The LayoutComponent constraints.
	 */
	public String getConstraints();

	/**
	 * Get a property which was set during creation on the LayoutComponent.
	 * 
	 * @param key
	 *           The name of the property to get.
	 * @return The property value, or null if no property exists with this name.
	 */
	public Object get( String key );

	/**
	 * Checks if the LayoutComponent has a property with the name indicated by
	 * key.
	 * 
	 * @param key
	 *           The name of the property to check for.
	 * @return True if the property exists, false if not.
	 */
	public boolean has( String key );
}
