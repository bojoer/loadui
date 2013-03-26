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
package com.eviware.loadui.api.statistics.store;

import com.eviware.loadui.api.traits.Deletable;

/**
 * Represents a value set which changes over time, allowing sequential reading
 * and writing.
 * 
 * @author dain.nilsson
 */
public interface Track extends Deletable
{
	/**
	 * Gets the Tracks ID, which needs to be unique within the Execution.
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Gets the parent Execution for the Track.
	 * 
	 * @return
	 */
	public Execution getExecution();

	/**
	 * Gets the TrackDescriptor describing the structure of this Track.
	 * 
	 * @return
	 */
	public TrackDescriptor getTrackDescriptor();

	/**
	 * Gets the closest succeeding Entry to the given time, for a source in the
	 * Track, measured in milliseconds since the start of the Execution.
	 * 
	 * @param timestamp
	 * @return
	 */
	public Entry getNextEntry( String source, long timestamp );

	/**
	 * @see getNextEntry(String, int)
	 */
	public Entry getNextEntry( String source, long timestamp, int interpolationLevel );

	/**
	 * Gets an Iterable over the specified range, including all Entries with a
	 * startTime <= timestamp <= endTime for the given source, where the times
	 * are given as milliseconds since the start of the Execution.
	 * 
	 * Interpolationlevel defaults to 0.
	 * 
	 * @param source
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Iterable<Entry> getRange( String source, long startTime, long endTime );

	/**
	 * @see getRange(String, int, int)
	 */
	public Iterable<Entry> getRange( String source, long startTime, long endTime, int interpolationLevel );
}
