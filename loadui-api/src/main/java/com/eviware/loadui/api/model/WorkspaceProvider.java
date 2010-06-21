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
package com.eviware.loadui.api.model;

import java.io.File;

/**
 * Loads a WorkspaceItem from a File, and provides a reference to the loaded
 * workspace.
 * 
 * @author dain.nilsson
 */
public interface WorkspaceProvider
{
	/**
	 * Loads a stored workspace from file.
	 * 
	 * @param workspaceFile
	 *           The location of the stored workspace.
	 * @return The loaded workspace.
	 */
	public WorkspaceItem loadWorkspace( File workspaceFile );

	/**
	 * Gets the current workspace, if one has been loaded.
	 * 
	 * @return The currently loaded WorkspaceItem.
	 */
	public WorkspaceItem getWorkspace();

	/**
	 * Checks if a workspace has been loaded.
	 * 
	 * @return True if a workspace has been loaded, false if not.
	 */
	public boolean isWorkspaceLoaded();
}
