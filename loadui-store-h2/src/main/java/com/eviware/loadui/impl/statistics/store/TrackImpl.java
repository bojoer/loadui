package com.eviware.loadui.impl.statistics.store;

import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;

public class TrackImpl implements Track
{

	private String id;
	private Execution execution;
	private TrackDescriptor trackDescriptor;

	public TrackImpl( String trackId, Execution execution, TrackDescriptor trackDescriptor )
	{
		super();
		this.id = trackId;
		this.execution = execution;
		this.trackDescriptor = trackDescriptor;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public Execution getExecution()
	{
		return execution;
	}

	@Override
	public TrackDescriptor getTrackDescriptor()
	{
		return trackDescriptor;
	}

	@Override
	public void write( Entry entry, String source )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Entry getLastEntry( String source )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry getNextEntry( String source, int timestamp )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Entry> getRange( String source, int startTime, int endTime )
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
