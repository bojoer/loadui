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
package com.eviware.loadui.api.messaging;

/**
 * A static class holding constants to be used for intra-Scene communication.
 * 
 * @author dain.nilsson
 */
public final class SceneCommunication
{
	private SceneCommunication()
	{
		throw new RuntimeException( "SceneCommunication should never be instantiated!" );
	}

	/**
	 * The base channel for all scene communication.
	 */
	public final static String CHANNEL = "/" + SceneCommunication.class.getName();

	/**
	 * Command for updating the scenes label.
	 */
	public final static String LABEL = "label";

	/**
	 * Command for adding a component.
	 */
	public final static String ADD_COMPONENT = "addComponent";

	/**
	 * Command for removing (deleting) a component.
	 */
	public final static String REMOVE_COMPONENT = "removeComponent";

	/**
	 * Command for adding a connection.
	 */
	public final static String CONNECT = "connect";

	/**
	 * Command for removing a connection.
	 */
	public final static String DISCONNECT = "disconnect";

	/**
	 * Command for firing an ActionEvent on the remote SceneItem.
	 */
	public final static String ACTION_EVENT = "actionEvent";

	/**
	 * Command for gathering Agent statistics for a completed SceneItem.
	 */
	public final static String COLLECT_STATISTICS = "collectStatistics";

	/**
	 * Command for canceling components.
	 */
	public final static String CANCEL_COMPONENTS = "cancelComponents";
}
