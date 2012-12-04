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

import javax.annotation.Nonnull;

import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.InputTerminal;

/**
 * A SceneItem is a CanvasItem, so it holds loadUI components. A SceneItem can
 * be assigned to one or several AgentItems, allowing it to run on a remote
 * loadUI agent.
 * 
 * @author dain.nilsson
 */
public interface SceneItem extends CanvasItem, CanvasObjectItem
{
	/**
	 * A Property<Boolean> used to store the state of followProject, used in
	 * isFollowProject and setFollowProject.
	 */
	public static final String FOLLOW_PROJECT_PROPERTY = SceneItem.class.getSimpleName() + ".followProject";

	/**
	 * Gets the version number of the SceneItem. Any time a change is made to the
	 * SceneItem, this will cause its version number to increment.
	 * 
	 * @return The version number of the SceneItem.
	 */
	public long getVersion();

	/**
	 * Gets the followProject property value.
	 * 
	 * @return
	 */
	public boolean isFollowProject();

	/**
	 * Gets the followProject property.
	 * 
	 * @return
	 */
	public Property<Boolean> followProjectProperty();

	/**
	 * TODO: Document
	 * 
	 * @param execution
	 * @return
	 */
	public boolean isAffectedByExecutionTask( TestExecution execution );

	/**
	 * Sets the followProject property value.
	 * 
	 * If set to false, this SceneItem will not propagate ActionEvents of type
	 * START or STOP fired on its ProjectItem to itself or its children. This
	 * will have the effect that the SceneItem will not react to the Project
	 * starting or stopping its execution.
	 * 
	 * @param followProject
	 */
	public void setFollowProject( boolean followProject );

	/**
	 * Broadcasts a message to all AgentItems currently assigned to this
	 * SceneItem.
	 * 
	 * @param channel
	 *           The channel over which to send the message.
	 * @param data
	 *           The data to send.
	 */
	public void broadcastMessage( @Nonnull String channel, Object data );

	/**
	 * Gets the InputTerminal controlling the SceneItems on/off state.
	 * 
	 * @return
	 */
	@Nonnull
	public InputTerminal getStateTerminal();
}
