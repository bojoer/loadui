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
package com.eviware.loadui.api.model;

import com.eviware.loadui.api.addon.AddonHolder;

/**
 * The base for all model items in loadUI.
 * 
 * @author dain.nilsson
 */
public interface ModelItem extends BaseItem, AddonHolder, AttributeHolder, PropertyHolder, Labeled.Mutable, Releasable
{
	// Properties
	public final String DESCRIPTION_PROPERTY = ModelItem.class.getSimpleName() + ".description";

	/**
	 * Gets the URL to a web site providing help for the ComponentBehavior, or
	 * null if no such web site exists.
	 * 
	 * @return The full URL of a help page for the ModelItem.
	 */
	public String getHelpUrl();

	/**
	 * Triggers a named action which can be listened for. Actions propagate down
	 * to child ModelItems according to the following:
	 * 
	 * WorkspaceItem > ProjectItem > SceneItem > ComponentItem.
	 * 
	 * @param actionName
	 *           The name of the action to trigger.
	 */
	public void triggerAction( String actionName );

	/**
	 * Gets the description of the ModelItem.
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * Sets the description for the ModelItem.
	 * 
	 * @param description
	 */
	public void setDescription( String description );
}
