package com.eviware.loadui.impl.statistics.store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.impl.statistics.store.table.model.DataTable;
import com.eviware.loadui.util.statistics.store.EntryImpl;

public class TrackImpl implements Track
{
	private final static Logger log = LoggerFactory.getLogger( TrackImpl.class );

	private final String id;
	private final Execution execution;
	private final TrackDescriptor trackDescriptor;
	private final ExecutionManagerImpl manager;

	public TrackImpl( String trackId, Execution execution, TrackDescriptor trackDescriptor, ExecutionManagerImpl manager )
	{
		this.id = trackId;
		this.execution = execution;
		this.trackDescriptor = trackDescriptor;
		this.manager = manager;
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
	public Entry getNextEntry( String source, int timestamp )
	{
		try
		{
			Map<String, Object> result = manager.readNext( execution.getId(), id, source, timestamp );
			if( result.size() > 0 )
			{
				Integer tstamp = ( Integer )result.get( DataTable.STATIC_FIELD_TIMESTAMP );
				Map<String, Number> values = new HashMap<String, Number>();
				Iterator<String> keys = result.keySet().iterator();
				while( keys.hasNext() )
				{
					String key = keys.next();
					if( !DataTable.STATIC_FIELD_TIMESTAMP.equalsIgnoreCase( key )
							&& !DataTable.STATIC_FIELD_SOURCEID.equalsIgnoreCase( key ) )
					{
						values.put( key, ( Number )result.get( key ) );
					}
				}
				return new EntryImpl( tstamp, values );
			}
			else
			{
				return null;
			}
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to retrieve next track entry!", e );
		}
	}

	@Override
	public Iterable<Entry> getRange( String source, int startTime, int endTime )
	{
		try
		{
			List<Map<String, Object>> queryResult = manager.read( execution.getId(), id, source, startTime, endTime );
			if( queryResult.size() > 0 )
			{
				List<Entry> resultList = new ArrayList<Entry>();
				for( int i = 0; i < queryResult.size(); i++ )
				{
					Map<String, Object> row = queryResult.get( i );
					Integer tstamp = ( Integer )row.get( DataTable.STATIC_FIELD_TIMESTAMP );
					Map<String, Number> values = new HashMap<String, Number>();
					Iterator<String> keys = row.keySet().iterator();
					while( keys.hasNext() )
					{
						String key = keys.next();
						if( !DataTable.STATIC_FIELD_TIMESTAMP.equalsIgnoreCase( key )
								&& !DataTable.STATIC_FIELD_SOURCEID.equalsIgnoreCase( key ) )
						{
							values.put( key, ( Number )row.get( key ) );
						}
					}
					resultList.add( new EntryImpl( tstamp, values ) );
				}
				return resultList;
			}
			else
			{
				return null;
			}
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to retrieve next track entry!", e );
		}
	}

	@Override
	public void delete()
	{
		try
		{
			manager.deleteTrack( execution.getId(), id );
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to delete track: " + id, e );
		}
	}
}