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

import java.util.Collection;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.api.summary.Summary;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;

/**
 * Base ModelItem which holds ComponentItems and Connections.
 * 
 * @author dain.nilsson
 */
public interface CanvasItem extends ModelItem, CounterHolder
{
	public static final String COMPONENTS = CanvasItem.class.getName() + "@components";
	public static final String CONNECTIONS = CanvasItem.class.getName() + "@connections";
	public static final String SUMMARY = CanvasItem.class.getName() + "@summary";
	public static final String LIMITS = CanvasItem.class.getName() + "@limits";

	public static final String START_ACTION = "START";
	public static final String STOP_ACTION = "STOP";
	public static final String COMPLETE_ACTION = "COMPLETE";
	public static final String READY_ACTION = "READY";

	public static final String TIMER_COUNTER = "Time";
	public static final String SAMPLE_COUNTER = "Samples";
	public static final String REQUEST_COUNTER = "Requests";
	public static final String FAILURE_COUNTER = "Failures";
	public static final String ASSERTION_COUNTER = "Assertions";

	/**
	 * Get the ProjectItem which this CanvasItem belongs to. If this CanvasItem
	 * is a ProjectItem itself, then it will return itself. Note that if this
	 * CanvasItem is a SceneItem deployed on an Agent, then it will not have a
	 * ProjectItem and will return null.
	 * 
	 * @return The ProjectItem which this CanvasItem belongs to.
	 */
	public ProjectItem getProject();

	/**
	 * Check whether the item has been changes since the last save.
	 * 
	 * @return True if it has been changed
	 */
	public boolean isDirty();

	/**
	 * Adds a new ComponentItem to this CanvasItem.
	 * 
	 * @param label
	 *           The label to give the new component.
	 * @param descriptor
	 *           The ComponentDescriptor to create a component from.
	 * @return The newly created ComponentItem.
	 */
	public ComponentItem createComponent( String label, ComponentDescriptor descriptor );

	/**
	 * Get the child components.
	 * 
	 * @return A Collection of all the contained Components.
	 */
	public Collection<ComponentItem> getComponents();

	/**
	 * Convenience method for finding a child ComponentItem with the given label.
	 * Returns null if no such ComponentItem exists.
	 * 
	 * @param label
	 * @return
	 */
	public ComponentItem getComponentByLabel( String label );

	/**
	 * Gets the connections in this canvas.
	 * 
	 * @return A Collection of the Connections in this CanvasItem.
	 */
	public Collection<Connection> getConnections();

	/**
	 * Connects an OutputTerminal to an InputTerminal, creating a new Connection.
	 * 
	 * @param output
	 *           The OutputTerminal to connect from.
	 * @param input
	 *           The InputTerminal to connect to.
	 * @return The newly created Connection.
	 */
	public Connection connect( OutputTerminal output, InputTerminal input );

	/**
	 * Gets the current running state of the CanvasItem.
	 * 
	 * @return True if the canvas is running, false if it is stopped.
	 */
	public boolean isRunning();

	/**
	 * Gets whether the cavas item has been started or not
	 * 
	 * @return True if the canvas has been started, even if it is paused, false
	 *         if it is stopped.
	 */
	public boolean isStarted();

	/**
	 * Set a limit for a Counter. When the given counter reaches the limit set,
	 * the CanvasItem is stopped.
	 * 
	 * @param counterName
	 * @param counterValue
	 */
	public void setLimit( String counterName, long counterValue );

	/**
	 * Get the currently set limit for the given Counter, or -1 if no limit has
	 * been set.
	 * 
	 * @param counterName
	 * @return
	 */
	public long getLimit( String counterName );

	/**
	 * Called on a CanvasItem to generate a summary of the last completed run.
	 * 
	 * @param summary
	 */
	public void generateSummary( MutableSummary summary );

	/**
	 * Gets the latest Summary. If no Summary is available, this will return
	 * null;
	 * 
	 * @return
	 */
	public Summary getSummary();

	/**
	 * Creates and returns a duplicate of the given CanvasObjectItem (which must
	 * already be a child of the CanvasItem).
	 * 
	 * @param obj
	 *           The child CanvasObjectItem to duplicate.
	 * 
	 * @return The new copy of the given object.
	 */
	public CanvasObjectItem duplicate( CanvasObjectItem obj );

	/**
	 * Used for checking if there were any errors when loading the component.
	 * 
	 * @return True if any errors occurred.
	 */
	public boolean isLoadingError();

	/**
	 * Triggers cancel messages for any Components within that are in a busy
	 * state.
	 */
	public void cancelComponents();
}
