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

/**
 * Represents a value set which changes over time, allowing sequential reading
 * and writing.
 * 
 * @author dain.nilsson
 */
public interface Track
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
	 * Gets the size of the value set, i.e. how many values are stored per Entry.
	 * 
	 * @return
	 */
	public int getValueCount();

	/**
	 * Stores a new Entry for the given timestamp using the given values.
	 * 
	 * @param timestamp
	 *           The current time since the start of the Execution given in
	 *           milliseconds.
	 * @param values
	 */
	public Entry storeEntry( int timestamp, Number... values );

	/**
	 * Gets the closest succeeding Entry in the Track to the given time, measured
	 * in milliseconds since the start of the Execution.
	 * 
	 * @param timestamp
	 * @return
	 */
	public Entry getNextEntry( int timestamp );

	/**
	 * Gets an Iterable over the specified range, including all Entries with a
	 * startTime <= timestamp <= endTime.
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Iterable<Entry> getRange( int startTime, int endTime );

	/**
	 * Deletes the Track from the Execution, removing all data from the
	 * underlying database.
	 */
	public void delete();
}
