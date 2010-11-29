package com.eviware.loadui.impl.statistics.store;

import java.sql.SQLException;
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

	public ExecutionImpl( String id, long timestamp, ExecutionManagerImpl manager )
	{
		this.id = id;
		this.startTime = timestamp;
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
		try
		{
			manager.delete( id );
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Failed to delete execution!", e );
		}
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
}