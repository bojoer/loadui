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
package com.eviware.loadui.api.layout;

import com.eviware.loadui.api.property.Property;

/**
 * A LayoutComponent which holds a Property value and makes it accessible from
 * the user interface.
 * 
 * @author dain.nilsson
 * @param <T>
 *           The type of the property.
 */
public interface PropertyLayoutComponent<T> extends LayoutComponent
{
	/**
	 * Accessor for the Property that is made available through this
	 * LayoutComponent.
	 * 
	 * @return The bound Property
	 */
	public Property<T> getProperty();

	/**
	 * If a PropertyLayoutComponent is marked as read only, its value should not
	 * be editable by the user through the user interface.
	 * 
	 * @return If the PropertyLayoutComponent is in ReadOnly mode.
	 */
	public boolean isReadOnly();

	/**
	 * Each PropertyLayoutComponent may optionally have a label attached to it.
	 * If the component does not have a label, this method will return null.
	 * 
	 * @return The label if one exists, otherwise null.
	 */
	public String getLabel();

	/**
	 * Optionally each PropertyLayoutComponent can provide hints regarding how to
	 * render the component. A Hint can specify which component to use for the
	 * Property, or if the component should be highlighted.
	 * 
	 * @return A String describing the hints to use for rendering. May be blank,
	 *         but not null.
	 */
	public String getHint();
}
