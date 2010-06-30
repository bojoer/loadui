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
import java.util.Collection;

/**
 * A loadUI Project. It is stored as a file on the disk.
 * 
 * @author dain.nilsson
 */
public interface ProjectItem extends CanvasItem
{
	public static final String SCENES = ProjectItem.class.getName() + "@scenes";
	public static final String ASSIGNMENTS = ProjectItem.class.getName() + "@assignments";

	/**
	 * Gets the File for this ProjectItem.
	 * 
	 * @return The File containing the stored project.
	 */
	public File getProjectFile();

	/**
	 * Saves the project to disk.
	 */
	public void save();

	/**
	 * Gets the workspace to which the project belongs.
	 * 
	 * @return The parent WorkspaceItem.
	 */
	public WorkspaceItem getWorkspace();

	/**
	 * Gets all contained scenes.
	 * 
	 * @return A Collection of contained SceneItems.
	 */
	public Collection<SceneItem> getScenes();

	/**
	 * Creates a new scene in the project.
	 * 
	 * @param label
	 *           The name to give the new SceneItem.
	 * @return The newly created SceneItem.
	 */
	public SceneItem createScene( String label );

	/**
	 * Gets all the runners which are currently assigned to a given scene.
	 * 
	 * @param scene
	 *           The SceneItem.
	 * @return A Collection of RunnerItems.
	 */
	public Collection<RunnerItem> getRunnersAssignedTo( SceneItem scene );

	/**
	 * Broadcasts a message to all RunnerItems currently assigned to the given
	 * SceneItem.
	 * 
	 * @param scene
	 *           The SceneItem to broadcast the message for.
	 * @param channel
	 *           The channel over which to send the message.
	 * @param data
	 *           The data to send.
	 */
	public void broadcastMessage( SceneItem scene, String channel, Object data );

	/**
	 * Gets all the scenes to which a given runner is assigned to.
	 * 
	 * @param runner
	 *           The RunnerItem.
	 * @return A Collection of SceneItems.
	 */
	public Collection<SceneItem> getScenesAssignedTo( RunnerItem runner );

	/**
	 * Gets all the scene-runner assignments.
	 * 
	 * @return A Collection of Assigments.
	 */
	public Collection<Assignment> getAssignments();

	/**
	 * Assigns a given runner to a given scene.
	 * 
	 * @param scene
	 *           The SceneItem to assign the RunnerItem to.
	 * @param runner
	 *           The RunnerItem to assign to the SceneItem.
	 */
	public void assignScene( SceneItem scene, RunnerItem runner );

	/**
	 * Unassigns a previously assigned runner to a scene.
	 * 
	 * @param scene
	 *           The SceneItem to unassign from the RunnerItem.
	 * @param runner
	 *           The RunnerItem to unassign.
	 */
	public void unassignScene( SceneItem scene, RunnerItem runner );
	
	public boolean isSaveReport();
	
	public void setSaveReport(boolean save);
}
