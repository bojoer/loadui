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
package com.eviware.loadui.api.model;

import java.io.File;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.eviware.loadui.api.property.Property;
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
	public static final String SCENE_LOADED = ProjectItem.class.getName() + "@sceneLoaded";

	public static final String SAVE_REPORT_PROPERTY = ModelItem.class.getSimpleName() + ".saveReport";
	public static final String REPORT_FOLDER_PROPERTY = ModelItem.class.getSimpleName() + ".reportFolder";
	public static final String REPORT_FORMAT_PROPERTY = ModelItem.class.getSimpleName() + ".reportFormat";

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
	@Nonnull
	public WorkspaceItem getWorkspace();

	/**
	 * Convenience method for finding a child SceneItem with the given label.
	 * Returns null if no such SceneItem exists.
	 * 
	 * @param label
	 * @return
	 */
	@Nullable
	public SceneItem getSceneByLabel( @Nonnull String label );

	/**
	 * Creates a new scene in the project.
	 * 
	 * @param label
	 *           The name to give the new SceneItem.
	 * @return The newly created SceneItem.
	 */
	@Nonnull
	public SceneItem createScene( @Nonnull String label );

	/**
	 * Gets all the agents which are currently assigned to a given scene.
	 * 
	 * @param scene
	 *           The SceneItem.
	 * @return A Collection of AgentItems.
	 */
	@Nonnull
	public Collection<? extends AgentItem> getAgentsAssignedTo( SceneItem scene );

	/**
	 * Checks to see if a given SceneItem is loaded on a particular AgentItem.
	 * 
	 * @param scene
	 * @param agent
	 * @return
	 */
	public boolean isSceneLoaded( @Nonnull SceneItem scene, @Nonnull AgentItem agent );

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
	public void broadcastMessage( @Nonnull SceneItem scene, @Nonnull String channel, Object data );

	/**
	 * Gets all the scenes to which a given agent is assigned to.
	 * 
	 * @param agent
	 *           The AgentItem.
	 * @return A Collection of SceneItems.
	 */
	@Nonnull
	public Collection<? extends SceneItem> getScenesAssignedTo( @Nonnull AgentItem agent );

	/**
	 * Gets all the scene-agent assignments.
	 * 
	 * @return A Collection of Assigments.
	 */
	@Nonnull
	public Collection<? extends Assignment> getAssignments();

	/**
	 * Assigns a given agent to a given scene.
	 * 
	 * @param scene
	 *           The SceneItem to assign the AgentItem to.
	 * @param agent
	 *           The AgentItem to assign to the SceneItem.
	 */
	public void assignScene( @Nonnull SceneItem scene, @Nonnull AgentItem agent );

	/**
	 * Unassigns a previously assigned agent to a scene.
	 * 
	 * @param scene
	 *           The SceneItem to unassign from the AgentItem.
	 * @param agent
	 *           The AgentItem to unassign.
	 */
	public void unassignScene( @Nonnull SceneItem scene, @Nonnull AgentItem agent );

	/**
	 * Checks if summaries are saved at the end of each run.
	 * 
	 * @return true if the summaries are saved.
	 */
	public boolean isSaveReport();

	/**
	 * Whether summary reports are saved at the end of each run.
	 */
	public Property<Boolean> saveReportProperty();

	/**
	 * Used to set whether the summary should be saved at the end of each run.
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
	public void saveAs( @Nonnull File dest );

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


}
