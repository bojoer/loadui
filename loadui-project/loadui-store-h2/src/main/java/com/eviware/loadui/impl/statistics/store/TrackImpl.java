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
package com.eviware.loadui.impl.statistics.store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
	public final static Logger log = LoggerFactory.getLogger( TrackImpl.class );

	private final String id;
	private final Execution execution;
	private final TrackDescriptor trackDescriptor;
	private final ExecutionManagerImpl manager;

	public TrackImpl( Execution execution, TrackDescriptor trackDescriptor, ExecutionManagerImpl manager )
	{
		this.id = trackDescriptor.getId();
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
		return getNextEntry( source, timestamp, 0 );
	}

	@Override
	public Entry getNextEntry( String source, int timestamp, int interpolationLevel )
	{
		try
		{
			Map<String, Object> result = manager.readNext( execution.getId(), id, source, timestamp, interpolationLevel );
			if( result.size() > 0 )
			{
				int tstamp = ( ( Long )result.get( DataTable.STATIC_FIELD_TIMESTAMP ) ).intValue();
				Map<String, Number> values = new HashMap<String, Number>();
				Iterator<String> keys = result.keySet().iterator();
				while( keys.hasNext() )
				{
					String key = keys.next();
					if( !DataTable.STATIC_FIELD_TIMESTAMP.equalsIgnoreCase( key )
							&& !DataTable.STATIC_FIELD_SOURCEID.equalsIgnoreCase( key )
							&& !DataTable.STATIC_FIELD_INTERPOLATIONLEVEL.equalsIgnoreCase( key ) )
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
		return getRange( source, startTime, endTime, 0 );
	}

	@Override
	public Iterable<Entry> getRange( String source, int startTime, int endTime, int interpolationLevel )
	{
		try
		{
			List<Map<String, Object>> queryResult = manager.read( execution.getId(), id, source, startTime, endTime,
					interpolationLevel );
			if( queryResult.size() > 0 )
			{
				List<Entry> resultList = new ArrayList<Entry>();
				for( int i = 0; i < queryResult.size(); i++ )
				{
					Map<String, Object> row = queryResult.get( i );
					Long tstamp = ( Long )row.get( DataTable.STATIC_FIELD_TIMESTAMP );
					Map<String, Number> values = new HashMap<String, Number>();
					Iterator<String> keys = row.keySet().iterator();
					while( keys.hasNext() )
					{
						String key = keys.next();
						if( !DataTable.STATIC_FIELD_TIMESTAMP.equalsIgnoreCase( key )
								&& !DataTable.STATIC_FIELD_SOURCEID.equalsIgnoreCase( key )
								&& !DataTable.STATIC_FIELD_INTERPOLATIONLEVEL.equalsIgnoreCase( key ) )
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
				return Collections.emptyList();
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