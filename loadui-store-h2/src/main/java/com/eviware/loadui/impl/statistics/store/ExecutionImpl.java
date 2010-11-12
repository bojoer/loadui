package com.eviware.loadui.impl.statistics.store;

import java.util.Collection;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;

public class ExecutionImpl implements Execution
{

	private String id;

	private long startTime;
	
	public ExecutionImpl( String id, long timestamp )
	{
		this.id = id;
		this.startTime = timestamp;
	}

	@Override
	public long getStartTime()
	{
		return startTime;
	}

	@Override
	public Track getTrack( String trackId )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getTrackIds()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete()
	{
		// TODO Auto-generated method stub

	}

}
