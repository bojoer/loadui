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
package com.eviware.loadui.api.statistics.store;

import java.util.Collection;
import java.util.Map;

/**
 * Manages existing Executions, creating new ones, etc.
 * 
 * @author dain.nilsson
 */
public interface ExecutionManager
{
	/**
	 * Gets the current Execution.
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
	 * Creates a new track in the current Execution with the given id and
	 * structure. If a Track with the given ID already exists, it will be
	 * returned.
	 * 
	 * @param trackId
	 * @param trackStructure
	 * @return
	 */
	public Track createTrack( String trackId, Map<String, Class<? extends Number>> trackStructure );

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
}
