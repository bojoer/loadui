package com.eviware.loadui.impl.statistics.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;

public class ExecutionImpl implements Execution
{

	private String id;

	private long startTime;

	private Map<String, Track> trackMap;

	public ExecutionImpl( String id, long timestamp )
	{
		this.id = id;
		this.startTime = timestamp;
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
		// TODO Auto-generated method stub

	}

	public Track createTrack( String trackId, TrackDescriptor td )
	{
		TrackImpl track = new TrackImpl( trackId, this, td );
		trackMap.put( trackId, track );
		return track;
	}
}