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

/**
 * Represents statistical data gathered during a load test Execution.
 * 
 * @author dain.nilsson
 */
public interface Execution
{
	/**
	 * Gets the Executions ID.
	 * 
	 * @return
	 */
	public String getId();
	
	/**
	 * Gets the start time of the test execution, measured as a Unix Timestamp
	 * given in milliseconds.
	 * 
	 * @return
	 */
	public long getStartTime();

	/**
	 * Gets the Track with the specified ID.
	 * 
	 * @param trackId
	 * @return
	 */
	public Track getTrack( String trackId );

	/**
	 * Gets a Collection of the IDs of the contained Tracks.
	 * 
	 * @return
	 */
	public Collection<String> getTrackIds();

	/**
	 * Deletes the Execution with all contained Track data.
	 */
	public void delete();
}
