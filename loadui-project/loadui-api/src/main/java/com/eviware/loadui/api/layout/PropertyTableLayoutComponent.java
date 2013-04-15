/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.api.layout;

import java.util.Collection;

import com.eviware.loadui.api.property.Property;

/**
 * A LayoutComponent which holds a Collection of properties and makes it
 * accessible from the user interface.
 * 
 * @author dain.nilsson
 */
public interface PropertyTableLayoutComponent extends LayoutComponent
{
	/**
	 * Each TableLayoutComponent may optionally have a label attached to it. If
	 * the component does not have a label, this method will return null.
	 * 
	 * @return The label if one exists, otherwise null.
	 */
	public String getLabel();

	/**
	 * Accessor for the Collection of items displayed in the list
	 * 
	 * @return The Collection if one exists, otherwise null.
	 */
	public Collection<Property<String>> getRows();
}
