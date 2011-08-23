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
package com.eviware.loadui.api.ui.inspector;

import java.util.Collection;

/**
 * A Panel which holds Inspectors.
 * 
 * @author dain.nilsson
 */
public interface InspectorPanel
{
	/**
	 * Adds an Inspector to the InspectorPanel. If the Inspector is already in
	 * the Panel, this does nothing.
	 * 
	 * @param inspector
	 *           The Inspector to add.
	 */
	public void addInspector( Inspector inspector );

	/**
	 * Removes an Inspector from the InspectorPanel. If the Inspector isn't
	 * attached to the Panel, this does nothing.
	 * 
	 * @param inspector
	 *           The Inspector to remove.
	 */
	public void removeInspector( Inspector inspector );

	/**
	 * Get a contained Inspector by its name.
	 * 
	 * @param name
	 *           The name of the Inspector.
	 * @return The Inspector with the name given, or null if no contained
	 *         Inspector has that name.
	 */
	public Inspector getInspector( String name );

	/**
	 * Get the unique id associated with this InspectorPanel.
	 * 
	 * @return This InspectorPanel's id.
	 */
	public String getId();

	/**
	 * Get a list of all contained Inspectors.
	 * 
	 * @return A Collection of all contained Inspectors.
	 */
	public Collection<Inspector> getInspectors();

	/**
	 * Causes the given Inspector to be selected and thus shown on the screen.
	 * This causes the previously shown Inspector to be hidden. The given
	 * Inspector must already be contained in the InspectorPanel, or this will
	 * throw an error.
	 * 
	 * @param inspector
	 *           The Inspector to show.
	 */
	public void selectInspector( Inspector inspector );

	/**
	 * Causes the InspectorPanel to collapse, hiding all the contained
	 * Inspectors. This will cause the active Inspector to be hidden. If the
	 * Panel is already in a collapsed state, calling this will have no effect.
	 */
	public void collapse();

	/**
	 * Causes the InspectorPanel to expand, going from a hidden state to visible.
	 * This will cause the active Inspector to be shown. If the Panel is already
	 * in an expanded state, calling this will have no effect.
	 */
	public void expand();

	/**
	 * Check if the current state of the InspectorPanel is expanded or collapsed.
	 * 
	 * @return true if the Panel is expanded, false if not.
	 */
	public boolean isExpanded();

}
