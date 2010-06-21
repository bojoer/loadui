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

import com.eviware.loadui.api.messaging.MessageEndpoint;

/**
 * A connection to a remote loadUI runner.
 * 
 * @author dain.nilsson
 */
public interface RunnerItem extends ModelItem, MessageEndpoint
{
	// Properties
	public final static String SCENES = RunnerItem.class.getName() + "@scenes";
	public final static String ENABLED = RunnerItem.class.getName() + "@enabled";
	public final static String READY = RunnerItem.class.getName() + "@ready";
	public final static String URL = RunnerItem.class.getName() + "@url";

	// Channels
	public final static String RUNNER_CHANNEL = "/" + RunnerItem.class.getName();

	// Commands
	public final static String ASSIGN = "assign";
	public final static String UNASSIGN = "unassign";
	public final static String STARTED = "started";
	public final static String DEFINE_SCENE = "defineScene";
	public final static String SCENE_DEFINITION = "sceneDefinition";
	public final static String SCENE_ID = "sceneId";
	public final static String SET_MAX_THREADS = "setMaxThreads";

	public final static String MAX_THREADS_PROPERTY = RunnerItem.class.getSimpleName() + ".maxThreads";

	/**
	 * Gets the workspace containing the RunnerItem.
	 * 
	 * @return The parent WorkspaceItem.
	 */
	public WorkspaceItem getWorkspace();

	/**
	 * Gets the URL of the remote runner.
	 * 
	 * @return The URL of the runner, as a String.
	 */
	public String getUrl();

	/**
	 * Sets the target URL of the runner.
	 * 
	 * @param url
	 *           The URL to connect to, as a String.
	 */
	public void setUrl( String url );

	/**
	 * Checks if the runner is enabled.
	 * 
	 * @return True if the RunnerItem is enabled, false if not.
	 */
	public boolean isEnabled();

	/**
	 * Sets the enabled status of the RunnerItem. An enabled RunnerItem will
	 * attempt to connect to the remote runner specified by its URL.
	 * 
	 * @param enabled
	 *           True to enable the RunnerItem, false to disable it.
	 */
	public void setEnabled( boolean enabled );

	/**
	 * Checks if the runner is connected and ready to transmit data.
	 * 
	 * @return True if the RunnerItem is connected, false if not.
	 */
	public boolean isReady();
}
