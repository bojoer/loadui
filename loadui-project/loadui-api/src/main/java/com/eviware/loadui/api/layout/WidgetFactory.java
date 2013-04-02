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

/**
 * A factory for creating Component Widgets to display.
 * 
 * @author dain.nilsson
 */
public interface WidgetFactory
{
	/**
	 * Each Widget type needs to have a unique identifying String, so that it is
	 * possible to target a specific WidgetFactory type when creating a Layout.
	 * 
	 * @return The unique String identifying the WidgetFactory.
	 */
	public String getId();

	/**
	 * Constructs a Widget given a LayoutComponent, if possible.
	 * 
	 * @param layoutComponent
	 * @return The constructed Widget, which should be an instance of either a
	 *         LayoutComponentNode, a JavaFX Node (preferably implementing
	 *         Resizable), or a JComponent.
	 * @throws WidgetCreationException
	 *            If the WidgetFactory is unable to construct a Widget for the
	 *            given LayoutComponent, a WidgetCreationException is thrown.
	 */
	public Object buildWidget( LayoutComponent layoutComponent ) throws WidgetCreationException;
}
