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

import com.eviware.loadui.api.messaging.MessageEndpoint;

/**
 * A connection to a remote loadUI agent.
 * 
 * @author dain.nilsson
 */
public interface AgentItem extends ModelItem, MessageEndpoint
{
	// Properties
	public final static String SCENES = AgentItem.class.getName() + "@scenes";
	public final static String ENABLED = AgentItem.class.getName() + "@enabled";
	public final static String READY = AgentItem.class.getName() + "@ready";
	public final static String URL = AgentItem.class.getName() + "@url";
	public final static String UTILIZATION = AgentItem.class.getName() + "@utilization";

	// Channels
	public final static String AGENT_CHANNEL = "/" + AgentItem.class.getName();

	// Commands
	public final static String CONNECTED = "connected";
	public final static String ASSIGN = "assign";
	public final static String UNASSIGN = "unassign";
	public final static String STARTED = "started";
	public final static String DEFINE_SCENE = "defineScene";
	public final static String PROJECT_ID = "projectId";
	public final static String SCENE_DEFINITION = "sceneDefinition";
	public final static String SCENE_ID = "sceneId";
	public final static String SCENE_START_TIME = "sceneStartTime";
	public final static String SCENE_END_TIME = "sceneEndTime";
	public final static String SET_MAX_THREADS = "setMaxThreads";
	public final static String SET_UTILIZATION = "setUtilization";
	public final static String TIME_CHECK = "timeCheck";

	public final static String MAX_THREADS_PROPERTY = AgentItem.class.getSimpleName() + ".maxThreads";

	/**
	 * Gets the workspace containing the AgentItem.
	 * 
	 * @return The parent WorkspaceItem.
	 */
	public WorkspaceItem getWorkspace();

	/**
	 * Gets the URL of the remote agent.
	 * 
	 * @return The URL of the agent, as a String.
	 */
	public String getUrl();

	/**
	 * Sets the target URL of the agent.
	 * 
	 * @param url
	 *           The URL to connect to, as a String.
	 */
	public void setUrl( String url );

	/**
	 * Checks if the agent is enabled.
	 * 
	 * @return True if the AgentItem is enabled, false if not.
	 */
	public boolean isEnabled();

	/**
	 * Sets the enabled status of the AgentItem. An enabled AgentItem will
	 * attempt to connect to the remote agent specified by its URL.
	 * 
	 * @param enabled
	 *           True to enable the AgentItem, false to disable it.
	 */
	public void setEnabled( boolean enabled );

	/**
	 * Checks if the agent is connected and ready to transmit data.
	 * 
	 * @return True if the AgentItem is connected, false if not.
	 */
	public boolean isReady();

	/**
	 * Gets a numeric value 0-100 representing the current utilization of the
	 * Agent, 0 meaning idle and 100 meaning running at full capacity.
	 * 
	 * @return
	 */
	public int getUtilization();

	/**
	 * Returns an approximation of the difference in system clocks between the
	 * controller and the agent in ms. A negative value means that the agents
	 * clock is ahead of the controllers, so the following can be used to convert
	 * an agent timestamp to a local one: localTime = agentTime + timeDifference.
	 * 
	 * @return
	 */
	public long getTimeDifference();

	/**
	 * Forces timer difference approximation to run.
	 */
	public void resetTimeDifference();
}
