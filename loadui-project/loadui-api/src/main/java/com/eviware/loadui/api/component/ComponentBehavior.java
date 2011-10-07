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
package com.eviware.loadui.api.component;

import java.util.Map;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.summary.MutableChapter;

/**
 * Defines the appearance and behavior of a Component. Each instance of
 * Component has an attached ComponentBehavior.
 * 
 * @author dain.nilsson
 */
public interface ComponentBehavior
{
	/**
	 * Called when another Components OutputTerminal has been connected to this
	 * Components InputTerminal.
	 * 
	 * @param output
	 *           An OutputTerminal belonging to another Component.
	 * @param input
	 *           The InputTerminal of this Component which was connected to.
	 */
	public void onTerminalConnect( OutputTerminal output, InputTerminal input );

	/**
	 * Called whenever the Connection between another Components OutputTerminal
	 * and an InputTerminal of this Component has been disconnected.
	 * 
	 * @param output
	 *           An OutputTerminal belonging to another Component.
	 * @param input
	 *           The InputTerminal of this Component which was disconnected from.
	 */
	public void onTerminalDisconnect( OutputTerminal output, InputTerminal input );

	/**
	 * Called when there is a signature change on another Components
	 * OutputTerminal which is connected to an InputTerminal of this Component.
	 * 
	 * @param output
	 *           The OutputTerminal that changes its signature.
	 * @param signature
	 *           The new signature of the OutputTerminal.
	 */
	public void onTerminalSignatureChange( OutputTerminal output, Map<String, Class<?>> signature );

	/**
	 * Called when a TerminalMessage is sent to an InputTerminal on this
	 * Component.
	 * 
	 * @param output
	 *           The sending OutputTerminal.
	 * @param input
	 *           The receiving InputTerminal of this Component.
	 * @param message
	 *           The message.
	 */
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message );

	/**
	 * Called when the Component is stopped, either because it is deleted or
	 * because it is being unloaded.
	 */
	public void onRelease();

	/**
	 * Gets a String representation of the base color to use when displaying this
	 * category of component, eg "#ff0000".
	 * 
	 * @return The color of the category.
	 */
	public String getColor();

	/**
	 * Gets the name of the category.
	 * 
	 * @return The name of the category.
	 */
	public String getCategory();

	/**
	 * Collects data to be used for statistics calculations. Invoked at the end
	 * of a test run on each assigned Agent.
	 * 
	 * @return Statistics data to be sent to the Controller.
	 */
	public Object collectStatisticsData();

	/**
	 * Handle remotely collected statistics data gathered for Agents on the
	 * controller.
	 */
	public void handleStatisticsData( Map<AgentItem, Object> statisticsData );

	/**
	 * Called on a ComponentItem to generate a summary of its run.
	 * 
	 * @param summary
	 */
	public void generateSummary( MutableChapter summary );
}
