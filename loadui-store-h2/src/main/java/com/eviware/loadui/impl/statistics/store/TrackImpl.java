package com.eviware.loadui.impl.statistics.store;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;

public class TrackImpl implements Track
{
	private final static Logger log = LoggerFactory.getLogger( TrackImpl.class );

	private final String id;
	private final Execution execution;
	private final TrackDescriptor trackDescriptor;
	private final Map<String, Entry> lastEntries = new HashMap<String, Entry>();

	public TrackImpl( String trackId, Execution execution, TrackDescriptor trackDescriptor )
	{
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
		// TODO Write to database.
		log.debug( "Writing Entry: {} to Source: {}", entry, source );

		if( !lastEntries.containsKey( source ) || lastEntries.get( source ).getTimestamp() < entry.getTimestamp() )
			lastEntries.put( source, entry );
	}

	@Override
	public Entry getLastEntry( String source )
	{
		return lastEntries.get( source );
	}

	@Override
	public Entry getNextEntry( String source, int timestamp )
	{
		// TODO Get data from the database
		return null;
	}

	@Override
	public Iterable<Entry> getRange( String source, int startTime, int endTime )
	{
		// TODO Get data from the database
		return Collections.emptySet();
	}

	@Override
	public void delete()
	{
		// TODO Remove from database.
	}
}