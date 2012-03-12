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
import java.util.Collection;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.discovery.AgentDiscovery.AgentReference;

/**
 * A loadUI workspace. It holds references to projects and agents.
 * 
 * @author dain.nilsson
 */
public interface WorkspaceItem extends ModelItem
{
	public final static String AGENTS = WorkspaceItem.class.getName() + "@agents";
	public final static String PROJECTS = WorkspaceItem.class.getName() + "@projects";
	public final static String PROJECT_REFS = WorkspaceItem.class.getName() + "@projectRefs";

	public final static String LOCAL_MODE_PROPERTY = WorkspaceItem.class.getSimpleName() + ".localMode";
	public final static String MAX_THREADS_PROPERTY = WorkspaceItem.class.getSimpleName() + ".maxThreads";
	public final static String MAX_THREAD_QUEUE_PROPERTY = WorkspaceItem.class.getSimpleName() + ".maxThreadQueue";

	public final static String IMPORT_MISSING_AGENTS_PROPERTY = WorkspaceItem.class.getSimpleName()
			+ ".importMissingAgents";

	public final static String SOAPUI_PATH_PROPERTY = WorkspaceItem.class.getSimpleName() + ".soapUIPath";
	public final static String SOAPUI_SYNC_PROPERTY = WorkspaceItem.class.getSimpleName() + ".soapUISync";
	public final static String SOAPUI_CAJO_PORT_PROPERTY = WorkspaceItem.class.getSimpleName() + ".soapUICajoPort";
	public final static String LOADUI_CAJO_PORT_PROPERTY = WorkspaceItem.class.getSimpleName() + ".loadUICajoPort";
	public final static String AUTO_GARBAGE_COLLECTION_INTERVAL = WorkspaceItem.class.getSimpleName()
			+ ".garbageCollectionInterval";

	public final static String STATISTIC_RESULTS_PATH = WorkspaceItem.class.getSimpleName() + ".statisticResultsPath";
	public final static String IGNORED_VERSION_UPDATE = WorkspaceItem.class.getSimpleName() + ".ignoredVersionUpdate";

	/**
	 * Gets the version of loadUI which was used to create the workspace.
	 * 
	 * @return The String representation of the version number.
	 */
	public String getLoaduiVersion();

	/**
	 * Gets the File for this workspace.
	 * 
	 * @return The File containing the workspace.
	 */
	public File getWorkspaceFile();

	/**
	 * Saves the current state of the WorkspaceItem to disk.
	 */
	public void save();

	/**
	 * Gets all the currently loaded Projects.
	 * 
	 * @return A Collection of the loaded ProjectItems.
	 */
	@Nonnull
	public Collection<? extends ProjectItem> getProjects();

	/**
	 * Gets all the referenced projects, as ProjectRefs. This will get both
	 * loaded/enabled ProjectRefs and unloaded/disabled ones.
	 * 
	 * @return A Collection of all the ProjectRefs in the WorkspaceItem.
	 */
	@Nonnull
	public Collection<? extends ProjectRef> getProjectRefs();

	/**
	 * Creates a new project in the workspace using the given non-existing File.
	 * 
	 * @param projectFile
	 *           The file to store the project in. Must not already exist!
	 * @param label
	 *           The name to give the new project.
	 * @param enabled
	 *           The initial state of the ProjectItem.
	 * @return The newly created ProjectItem.
	 */
	public ProjectItem createProject( File projectFile, String label, boolean enabled );

	/**
	 * Imports an existing project into the workspace.
	 * 
	 * @param projectFile
	 *           The File in which the project is stored.
	 * @param enabled
	 *           Whether to enable the ProjectRef (and load the ProjectItem)
	 *           initially or not.
	 * @return A ProjectRef referencing the newly imported project.
	 * @throws IOException
	 */
	public ProjectRef importProject( File projectFile, boolean enabled ) throws IOException;

	/**
	 * Removes a project from the workspace.
	 * 
	 * @param project
	 *           A ProjectRef in the workspace.
	 */
	public void removeProject( ProjectRef project );

	/**
	 * Removes a project from the workspace.
	 * 
	 * @param project
	 *           A ProjectItem in the workspace.
	 */
	public void removeProject( ProjectItem project );

	/**
	 * Gets all the contained AgentItems.
	 * 
	 * @return A Collection of contained AgentItems.
	 */
	@Nonnull
	public Collection<? extends AgentItem> getAgents();

	/**
	 * Creates a new AgentItem in the workspace.
	 * 
	 * @param url
	 *           The target URL of the remote loadUI agent.
	 * @param label
	 *           The name to give the local AgentItem.
	 * @return The newly created AgentItem.
	 */
	public AgentItem createAgent( String url, String label );

	/**
	 * Creates a new AgentItem in the workspace.
	 * 
	 * @param ref
	 *           The AgentReference to create a agent from.
	 * @param label
	 *           The name to give the local AgentItem.
	 * @return The newly created AgentItem.
	 */
	public AgentItem createAgent( AgentReference ref, String label );

	/**
	 * Removes a AgentItem from the workspace.
	 * 
	 * @param agent
	 *           The AgentItem to remove.
	 */
	public void removeAgent( AgentItem agent );

	/**
	 * Gets the localMode property value.
	 * 
	 * @return
	 */
	public boolean isLocalMode();

	/**
	 * Sets the localMode property value.
	 * 
	 * When in local mode, any SceneItems on the controller will act as if they
	 * were deployed on a local AgentItem. TerminalMessages and ActionEvents will
	 * not be propagated to any remote Agents when in this state.
	 * 
	 * @param localMode
	 */
	public void setLocalMode( boolean localMode );
}
