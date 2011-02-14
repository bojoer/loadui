/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.api.statistics.store;

import java.util.Collection;

import com.eviware.loadui.api.events.EventFirer;

/**
 * Manages existing Executions, creating new ones, etc.
 * 
 * @author dain.nilsson
 */
public interface ExecutionManager extends EventFirer
{
	/**
	 * CollectionEvent key for Executions.
	 */
	public static final String EXECUTIONS = ExecutionManager.class.getName() + "@executions";

	/**
	 * Gets the current Execution. Returns null if no Execution is currently
	 * running.
	 * 
	 * @return
	 */
	public Execution getCurrentExecution();

	/**
	 * Creates and starts a new Execution, making it current.
	 * 
	 * @param executionId
	 * @param startTime
	 * @return
	 */
	public Execution startExecution( String executionId, long startTime );

	/**
	 * Pauses current execution.
	 * 
	 * @return
	 */
	public void pauseExecution();

	/**
	 * Stops current execution.
	 * 
	 * @return
	 */
	public void stopExecution();

	/**
	 * Registers a TrackDescriptor, providing the structure of a Track.
	 * 
	 * @param trackId
	 * @param trackDescriptor
	 */
	public void registerTrackDescriptor( TrackDescriptor trackDescriptor );

	/**
	 * Unregisters a TrackDescriptor.
	 * 
	 * @param trackId
	 */
	public void unregisterTrackDescriptor( String trackId );

	/**
	 * Creates a new track in the current Execution with the given id and
	 * structure. If a Track with the given ID already exists, it will be
	 * returned.
	 * 
	 * @param trackId
	 * @param trackStructure
	 * @return
	 */
	public Track getTrack( String trackId );

	/**
	 * Gets a Collection of all registered TrackDescriptors.
	 * 
	 * @return
	 */
	public Collection<String> getTrackIds();

	/**
	 * Writes an Entry to the Track for the specified source. Instead of calling
	 * this method directly, usually the at( int ) method is used.
	 * 
	 * @param entry
	 */
	public void writeEntry( String trackId, Entry entry, String source );

	/**
	 * @see writeEntry(String, Entry, String)
	 */
	public void writeEntry( String trackId, Entry entry, String source, int interpolationLevel );

	/**
	 * Gets the last stored Entry for a particular source, which is cached in
	 * memory.
	 * 
	 * @return
	 */
	public Entry getLastEntry( String trackId, String source );

	/**
	 * @see Entry(String, String)
	 */
	public Entry getLastEntry( String trackId, String source, int interpolationLevel );

	/**
	 * Gets a list of the names of all available Executions.
	 * 
	 * @return
	 */
	public Collection<String> getExecutionNames();

	/**
	 * Gets a reference to a specific Execution by its ID.
	 * 
	 * @param executionId
	 * @return
	 */
	public Execution getExecution( String executionId );

	/**
	 * Add execution listener
	 * 
	 * @param el
	 */
	void addExecutionListener( ExecutionListener el );

	/**
	 * remove all listeners
	 */
	void removeAllExecutionListeners();

	/**
	 * removes added execution listener
	 */
	void removeExecutionListener( ExecutionListener el );

	/**
	 * ExecutionManager States, based on execution events. state of manager
	 * should be handled internaly ( setting the state )
	 * 
	 * @author robert
	 * 
	 */
	enum State
	{
		STARTED, PAUSED, STOPPED
	}

	/**
	 * Return current state of ExecutionManager
	 */
	State getState();

}
