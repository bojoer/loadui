/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.statistics.store;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.impl.statistics.db.table.model.DataTable;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class TrackImpl implements Track
{
	public final static Logger log = LoggerFactory.getLogger( TrackImpl.class );

	private final String id;
	private final ExecutionImpl execution;
	private final TrackDescriptor trackDescriptor;
	private final ExecutionManagerImpl manager;

	public TrackImpl( ExecutionImpl execution, TrackDescriptor trackDescriptor, ExecutionManagerImpl manager )
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
	public Entry getNextEntry( String source, long timestamp )
	{
		return getNextEntry( source, timestamp, 0 );
	}

	@Override
	public Entry getNextEntry( String source, long timestamp, int interpolationLevel )
	{
		try
		{
			Map<String, Object> result = manager.readNext( execution.getId(), id, source, timestamp, interpolationLevel );
			if( result.size() > 0 )
			{
				int tstamp = ( ( Long )result.get( DataTable.STATIC_FIELD_TIMESTAMP ) ).intValue();
				Map<String, Number> values = new HashMap<>();
				Iterator<java.util.Map.Entry<String, Object>> entries = result.entrySet().iterator();
				while( entries.hasNext() )
				{
					java.util.Map.Entry<String, Object> entry = entries.next();
					if( !DataTable.STATIC_FIELD_TIMESTAMP.equalsIgnoreCase( entry.getKey() ) )
					{
						values.put( entry.getKey(), ( Number )entry.getValue() );
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
	public Iterable<Entry> getRange( String source, long startTime, long endTime )
	{
		return getRange( source, startTime, endTime, 0 );
	}

	@Override
	public Iterable<Entry> getRange( String source, long startTime, long endTime, int interpolationLevel )
	{
		try
		{
			return Iterables.transform(
					manager.read( execution.getId(), id, source, startTime, endTime, interpolationLevel ),
					new Function<Map<String, Object>, Entry>()
					{
						@Override
						public Entry apply( Map<String, Object> entryData )
						{
							Long timestamp = ( Long )entryData.remove( DataTable.STATIC_FIELD_TIMESTAMP );

							return new EntryImpl( timestamp, Maps.transformValues( entryData, new Function<Object, Number>()
							{
								@Override
								public Number apply( Object value )
								{
									return ( Number )value;
								}
							} ) );
						}
					} );
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
