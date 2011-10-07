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
import java.util.Collection;

import com.eviware.loadui.api.statistics.model.StatisticPages;

/**
 * A loadUI Project. It is stored as a file on the disk.
 * 
 * @author dain.nilsson
 */
public interface ProjectItem extends CanvasItem
{
	public static final String SCENES = ProjectItem.class.getName() + "@scenes";
	public static final String ASSIGNMENTS = ProjectItem.class.getName() + "@assignments";
	public static final String SUMMARY_EXPORTED = ProjectItem.class.getName() + "@summaryExported";
	public static final String SCENE_LOADED = ProjectItem.class.getName() + "@sceneLoaded";

	public static final String SAVE_REPORT_PROPERTY = ModelItem.class.getSimpleName() + ".saveReport";
	public static final String REPORT_FOLDER_PROPERTY = ModelItem.class.getSimpleName() + ".reportFolder";
	public static final String REPORT_FORMAT_PROPERTY = ModelItem.class.getSimpleName() + ".reportFormat";
	public static final String STATISTIC_NUMBER_OF_AUTOSAVES = ProjectItem.class.getSimpleName()
			+ ".statisticNumberOfAutosaves";

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
	 * Convenience method for finding a child SceneItem with the given label.
	 * Returns null if no such SceneItem exists.
	 * 
	 * @param label
	 * @return
	 */
	public SceneItem getSceneByLabel( String label );

	/**
	 * Creates a new scene in the project.
	 * 
	 * @param label
	 *           The name to give the new SceneItem.
	 * @return The newly created SceneItem.
	 */
	public SceneItem createScene( String label );

	/**
	 * Gets all the agents which are currently assigned to a given scene.
	 * 
	 * @param scene
	 *           The SceneItem.
	 * @return A Collection of AgentItems.
	 */
	public Collection<AgentItem> getAgentsAssignedTo( SceneItem scene );

	/**
	 * Checks to see if a given SceneItem is loaded on a particular AgentItem.
	 * 
	 * @param scene
	 * @param agent
	 * @return
	 */
	public boolean isSceneLoaded( SceneItem scene, AgentItem agent );

	/**
	 * Broadcasts a message to all AgentItems currently assigned to the given
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
	 * Gets all the scenes to which a given agent is assigned to.
	 * 
	 * @param agent
	 *           The AgentItem.
	 * @return A Collection of SceneItems.
	 */
	public Collection<SceneItem> getScenesAssignedTo( AgentItem agent );

	/**
	 * Gets all the scene-agent assignments.
	 * 
	 * @return A Collection of Assigments.
	 */
	public Collection<Assignment> getAssignments();

	/**
	 * Assigns a given agent to a given scene.
	 * 
	 * @param scene
	 *           The SceneItem to assign the AgentItem to.
	 * @param agent
	 *           The AgentItem to assign to the SceneItem.
	 */
	public void assignScene( SceneItem scene, AgentItem agent );

	/**
	 * Unassigns a previously assigned agent to a scene.
	 * 
	 * @param scene
	 *           The SceneItem to unassign from the AgentItem.
	 * @param agent
	 *           The AgentItem to unassign.
	 */
	public void unassignScene( SceneItem scene, AgentItem agent );

	/**
	 * Checks if summaries are saved at the end of each run
	 * 
	 * @return true if the summaries are saved.
	 */
	public boolean isSaveReport();

	/**
	 * Used to set whether the summary should be saved at the end of each run
	 * 
	 * @param save
	 *           true if the reports should be saved
	 */
	public void setSaveReport( boolean save );

	/**
	 * Used to save project to some file.
	 * 
	 * @param dest
	 *           file where to save project
	 */
	public void saveAs( File dest );

	/**
	 * The folder for saving summaries
	 * 
	 * @return the path to the folder
	 */
	public String getReportFolder();

	/**
	 * Used to set the foldr for saving the summaries
	 * 
	 * @param path
	 *           The path to the folder
	 */
	public void setReportFolder( String path );

	/**
	 * The format for saving summaries (pdf, doc, rtf, xml etc.)
	 * 
	 * @return the format in which the report will be saved
	 */
	public String getReportFormat();

	/**
	 * Used to set the format for saving the summaries
	 * 
	 * @param format
	 *           The format in which the report will be saved
	 */
	public void setReportFormat( String format );

	/**
	 * Gets the StatisticPages associated with the Project.
	 * 
	 * @return
	 */
	public StatisticPages getStatisticPages();

	/**
	 * Cancels components of scenes assigned to this project.
	 * 
	 * @param linkedOnly
	 *           If true only linked scenes will be canceled. Otherwise all
	 *           scenes will be canceled.
	 */
	public void cancelScenes( boolean linkedOnly );

	/**
	 * Gets the maximum number of executions of this project that are
	 * automatically saved before the oldest one is deleted.
	 * 
	 * @return
	 */
	long getNumberOfAutosaves();

	/**
	 * Sets the maximum number of executions of this project that are
	 * automatically saved before the oldest one is deleted.
	 * 
	 * @return
	 */
	void setNumberOfAutosaves( long n );
}
