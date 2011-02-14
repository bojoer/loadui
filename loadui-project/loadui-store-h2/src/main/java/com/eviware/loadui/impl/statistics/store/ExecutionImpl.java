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
package com.eviware.loadui.impl.statistics.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;

/**
 * Execution implementation
 * 
 * @author predrag.vucetic
 */
public class ExecutionImpl implements Execution
{
	/**
	 * Execution id
	 */
	private final String id;

	/**
	 * Execution start time (in milliseconds since January 1st, 1970)
	 */
	private final long startTime;

	/**
	 * Reference to execution manager implementation
	 */
	private final ExecutionManagerImpl manager;

	/**
	 * Map that holds references to all tracks that belongs to this execution
	 */
	private Map<String, Track> trackMap;

	private boolean archived = false;

	/**
	 * Execution custom label
	 */
	private String label = null;
	
	/**
	 * Execution length
	 */
	private long length = 0;

	public ExecutionImpl( String id, long timestamp, long length, boolean archived, String label, ExecutionManagerImpl manager )
	{
		this.id = id;
		this.startTime = timestamp;
		this.archived = archived;
		this.label = label;
		this.length = length;
		this.manager = manager;
		trackMap = new HashMap<String, Track>();
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public long getStartTime()
	{
		return startTime;
	}

	@Override
	public Track getTrack( String trackId )
	{
		return trackMap.get( trackId );
	}

	@Override
	public Collection<String> getTrackIds()
	{
		return trackMap.keySet();
	}

	@Override
	public void delete()
	{
		manager.delete( id );
	}

	/**
	 * Adds track to track map after it was created in execution manager
	 * 
	 * @param track
	 *           Track to add to track map
	 */
	public void addTrack( Track track )
	{
		trackMap.put( track.getId(), track );
	}

	@Override
	public boolean isArchived()
	{
		return archived;
	}

	@Override
	public void archive()
	{
		if( !archived )
		{
			manager.archiveExecution( getId() );
			archived = true;
		}
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public void setLabel( String label )
	{
		manager.setExecutionLabel( getId(), label );
		this.label = label;
	}
	
	@Override
	public void setLength( long length )
	{
		manager.setExecutionLength( getId(), length );
		this.length = length;
	}

	@Override
	public long getLength()
	{
		return length;
	}
}