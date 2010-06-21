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
package com.eviware.loadui.api.component;

import java.net.URI;

/**
 * An immutable data structure describing a type of Component.
 * 
 * @author dain.nilsson
 */
public class ComponentDescriptor
{
	private final String type;
	private final String category;
	private final String label;
	private final String description;
	private final URI icon;

	/**
	 * Constructs a new ComponentDescriptor using the given values.
	 * 
	 * @param type
	 *           The type of the Component.
	 * @param category
	 *           The Category of the ComponentDescriptor.
	 * @param label
	 *           The label for the ComponentDescriptor.
	 * @param description
	 *           A description of the Component.
	 * @param icon
	 *           A File containing an icon image to display for the
	 *           ComponentDescriptor.
	 */
	public ComponentDescriptor( String type, String category, String label, String description, URI icon )
	{
		this.type = type;
		this.category = category;
		this.label = label;
		this.description = description;
		this.icon = icon;
	}

	public String getType()
	{
		return type;
	}

	public String getCategory()
	{
		return category;
	}

	public String getLabel()
	{
		return label;
	}

	public String getDescription()
	{
		return description;
	}

	public URI getIcon()
	{
		return icon;
	}
}
