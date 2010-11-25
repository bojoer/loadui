package com.eviware.loadui.impl.statistics.store;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;

public class ExecutionImpl implements Execution
{
	private final String id;

	private final long startTime;

	private final ExecutionManagerImpl manager;

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

	public void addTrack( Track track )
	{
		trackMap.put( track.getId(), track );
	}
}