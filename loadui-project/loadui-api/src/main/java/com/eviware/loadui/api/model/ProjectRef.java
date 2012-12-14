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

import java.io.File;
import java.io.IOException;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.traits.Labeled;

/**
 * A reference to a ProjectItem. Used by the workspace to keep track of project
 * files.
 * 
 * @author dain.nilsson
 */
public interface ProjectRef extends EventFirer, AttributeHolder, Labeled.Mutable
{
	public static final String LOADED = ProjectRef.class.getName() + "@loaded";
	public static final String UNLOADED = ProjectRef.class.getName() + "@unloaded";

	/**
	 * Returns the id of the ProjectItem to which this ProjectRef points.
	 * 
	 * @return
	 */
	public String getProjectId();

	/**
	 * Checks if the project is enabled.
	 * 
	 * @return True if the ProjectRef is enabled, false if not.
	 */
	public boolean isEnabled();

	/**
	 * Sets the enabled status of the ProjectRef. An enabled ProjectRef will
	 * attempt to load its ProjectItem.
	 * 
	 * @param enabled
	 *           True to enable, false to disable.
	 */
	public void setEnabled( boolean enabled ) throws IOException;

	/**
	 * Gets the File where the project is stored on disk.
	 * 
	 * @return The File containing the ProjectItem.
	 */
	public File getProjectFile();

	/**
	 * Gets the referenced project. This requires that the ProjectRef be enabled.
	 * 
	 * @return The referenced ProjectItem.
	 */
	public ProjectItem getProject();

	/**
	 * Removes the ProjectRef from the workspace, optionally deleting the project
	 * file as well.
	 * 
	 * @param deleteFile
	 *           True to also delete the project file from disk.
	 */
	public void delete( boolean deleteFile );
}
