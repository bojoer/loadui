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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionListener;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.impl.statistics.store.table.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.store.table.DataSourceProvider;
import com.eviware.loadui.impl.statistics.store.table.TableBase;
import com.eviware.loadui.impl.statistics.store.table.model.DataTable;
import com.eviware.loadui.impl.statistics.store.table.model.MetaDatabaseMetaTable;
import com.eviware.loadui.impl.statistics.store.table.model.MetaTable;
import com.eviware.loadui.impl.statistics.store.table.model.SequenceTable;
import com.eviware.loadui.impl.statistics.store.table.model.SourceTable;
import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;
import com.eviware.loadui.util.statistics.store.ExecutionChangeSupport;

/**
 * Implementation of execution manager. Basically main class for data handling.
 * Handles tables, database data sources etc.
 * 
 * @author predrag.vucetic
 * 
 */
public abstract class ExecutionManagerImpl implements ExecutionManager, DataSourceProvider
{

	/**
	 * The name of the meta-database
	 */
	private static final String METADATABASE_NAME = "__meta_database";

	/**
	 * Postfix added to data table name when creating source table
	 */
	private static final String SOURCE_TABLE_NAME_POSTFIX = "_sources";

	/**
	 * Meta table in meta database. Keeps common information: execution names,
	 * start times etc.
	 */
	private MetaDatabaseMetaTable metaDatabaseMetaTable;

	/**
	 * Current execution
	 */
	private ExecutionImpl currentExecution;

	private ExecutionChangeSupport ecs = new ExecutionChangeSupport();

	private Map<String, Execution> executionMap = new HashMap<String, Execution>();

	private final Map<String, TrackDescriptor> trackDescriptors = new HashMap<String, TrackDescriptor>();

	private final Map<String, Entry> latestEntries = new HashMap<String, Entry>();

	private TableRegistry tableRegistry = new TableRegistry();

	private DatabaseMetadata metadata;

	private ConnectionRegistry connectionRegistry;

	private Logger logger = LoggerFactory.getLogger( ExecutionManagerImpl.class );

	private State executionState = State.STOPPED;

	public ExecutionManagerImpl()
	{
		connectionRegistry = new ConnectionRegistry( this );

		metadata = new DatabaseMetadata();
		initializeDatabaseMetadata( metadata );

		metaDatabaseMetaTable = new MetaDatabaseMetaTable( METADATABASE_NAME, connectionRegistry, metadata, tableRegistry );

		addExecutionListener( new ExecutionListenerAdapter()
		{
			@Override
			public void executionStarted( ExecutionManager.State oldState )
			{
				// if new execution was just started create track from all track
				// descriptors
				if( oldState == ExecutionManagerImpl.State.STOPPED )
				{
					for( TrackDescriptor td : trackDescriptors.values() )
					{
						createTrack( td );
					}
				}
			}

			@Override
			public void trackRegistered( TrackDescriptor trackDescriptor )
			{
				// this is in case component is added during the test
				if( executionState == State.STARTED )
				{
					createTrack( trackDescriptor );
				}
			}

			@Override
			public void trackUnregistered( TrackDescriptor trackDescriptor )
			{
				// TODO delete track? If yes then do it just during test run or
				// always?
			}

		} );
	}

	@Override
	public Execution startExecution( String id, long timestamp )
	{
		// unpause if paused otherwise try to create new
		if( executionState == State.PAUSED )
		{
			executionState = State.STARTED;
			ecs.fireExecutionStarted( State.PAUSED );
			logger.debug( "State changed: PAUSED -> STARTED" );
			return currentExecution;
		}

		latestEntries.clear();

		try
		{
			if( metaDatabaseMetaTable.exist( id ) )
			{
				throw new IllegalArgumentException( "Execution with the specified id already exist!" );
			}
			// create sequence table
			SequenceTable sequenceTable = new SequenceTable( id, connectionRegistry, metadata, tableRegistry );
			tableRegistry.put( id, sequenceTable );

			MetaTable metaTable = new MetaTable( id, connectionRegistry, metadata, tableRegistry );
			tableRegistry.put( id, metaTable );

			HashMap<String, Object> m = new HashMap<String, Object>();
			m.put( MetaDatabaseMetaTable.STATIC_FIELD_EXECUTION_NAME, id );
			m.put( MetaDatabaseMetaTable.STATIC_FIELD_TSTAMP, timestamp );
			metaDatabaseMetaTable.insert( m );

			currentExecution = new ExecutionImpl( id, timestamp, this );
			executionMap.put( id, currentExecution );

			executionState = State.STARTED;
			ecs.fireExecutionStarted( State.STOPPED );
			logger.debug( "State changed: STOPPED -> STARTED" );

			return currentExecution;
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Error while writing execution data to the database!", e );
		}
	}

	@Override
	public Execution getCurrentExecution()
	{
		return currentExecution;
	}

	@Override
	public Track getTrack( String trackId )
	{
		if( currentExecution == null )
		{
			throw new IllegalArgumentException( "There is no running execution!" );
		}
		Track track = currentExecution.getTrack( trackId );
		if( track == null )
		{
			throw new IllegalArgumentException( "No track found for specified trackId!" );
		}
		return track;
	}

	private synchronized void createTrack( TrackDescriptor td )
	{
		// synchronized to make sure that all tracks are created one by one, and
		// if one track is in process of creation to stop other threads to try to
		// create the same track. If one thread creates a track, others will have
		// to wait and when synchronized block is exited new track will already be
		// placed into currentExecution so another threads threads will know that
		// this track was created and will exit this method.
		if( currentExecution.getTrack( td.getId() ) != null )
		{
			return;
		}
		try
		{
			String executionId = currentExecution.getId();

			// create data table
			DataTable dtd = new DataTable( executionId, td.getId(), td.getValueNames(), connectionRegistry, metadata,
					tableRegistry );

			// create sources table
			SourceTable std = new SourceTable( executionId, td.getId() + SOURCE_TABLE_NAME_POSTFIX, connectionRegistry,
					metadata, tableRegistry );
			dtd.setParentTable( std );

			tableRegistry.put( executionId, dtd );
			tableRegistry.put( executionId, std );

			// insert into meta-table
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( MetaTable.STATIC_FIELD_TRACK_NAME, td.getId() );
			TableBase metaTable = tableRegistry.getTable( executionId, MetaTable.METATABLE_NAME );
			metaTable.insert( data );

			// if all tables are created properly and meta data
			// inserted, create track instance and add it to current
			// execution
			Track track = new TrackImpl( currentExecution, td, this );
			currentExecution.addTrack( track );
		}
		catch( SQLException e )
		{
			// TODO drop dtd and std tables if they are created and delete
			// record from meta-table if inserted (and remove from current
			// execution if added, but this actually shouldn't occur)
			throw new RuntimeException( "Unable to create track!", e );
		}
	}

	@Override
	public Collection<String> getExecutionNames()
	{
		try
		{
			return metaDatabaseMetaTable.list();
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Error while trying to fetch execution names from the database!", e );
		}
	}

	@Override
	public Execution getExecution( String executionId )
	{
		if( executionMap.containsKey( executionId ) )
		{
			return executionMap.get( executionId );
		}
		else
		{
			try
			{
				Map<String, Object> data = new HashMap<String, Object>();
				data.put( MetaDatabaseMetaTable.SELECT_ARG_EXECUTION_NAME_EQ, executionId );
				data = metaDatabaseMetaTable.selectFirst( data );
				if( data.size() == 0 )
				{
					throw new IllegalArgumentException( "Execution with the specified id does not exist!" );
				}
				else
				{
					return new ExecutionImpl( ( String )data.get( MetaDatabaseMetaTable.STATIC_FIELD_EXECUTION_NAME ),
							( Long )data.get( MetaDatabaseMetaTable.STATIC_FIELD_TSTAMP ), this );
				}
			}
			catch( SQLException e )
			{
				throw new RuntimeException( "Error while trying to fetch execution data from the database!", e );
			}
		}
	}

	@Override
	public void registerTrackDescriptor( TrackDescriptor trackDescriptor )
	{
		trackDescriptors.put( trackDescriptor.getId(), trackDescriptor );
		ecs.fireTrackRegistered( trackDescriptor );
	}

	@Override
	public void unregisterTrackDescriptor( String trackId )
	{
		ecs.fireTrackUnregistered( trackDescriptors.remove( trackId ) );
	}

	@Override
	public Collection<String> getTrackIds()
	{
		return Collections.unmodifiableSet( trackDescriptors.keySet() );
	}

	@Override
	public void writeEntry( String trackId, Entry entry, String source )
	{
		writeEntry( trackId, entry, source, 0 );
	}

	@Override
	public void writeEntry( String trackId, Entry entry, String source, int interpolationLevel )
	{
		latestEntries.put( trackId + ":" + source + ":" + String.valueOf( interpolationLevel ), entry );

		Execution execution = getCurrentExecution();
		if( execution != null )
		{
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( DataTable.STATIC_FIELD_TIMESTAMP, entry.getTimestamp() );
			data.put( DataTable.STATIC_FIELD_INTERPOLATIONLEVEL, interpolationLevel );
			Collection<String> nameCollection = entry.getNames();
			for( Iterator<String> iterator = nameCollection.iterator(); iterator.hasNext(); )
			{
				String name = iterator.next();
				data.put( name, entry.getValue( name ) );
			}
			try
			{
				write( execution.getId(), trackId, source, data );
			}
			catch( SQLException e )
			{
				throw new RuntimeException( "Unable to write data to the database!", e );
			}
		}
	}

	@Override
	public Entry getLastEntry( String trackId, String source )
	{
		return getLastEntry( trackId, source, 0 );
	}

	@Override
	public Entry getLastEntry( String trackId, String source, int interpolationLevel )
	{
		return latestEntries.get( trackId + ":" + source + ":" + String.valueOf( interpolationLevel ) );
	}

	public void clearMetaDatabase()
	{
		try
		{
			metaDatabaseMetaTable.delete();
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to clear the meta database!", e );
		}
	}

	private void write( String executionId, String trackId, String source, Map<String, Object> data )
			throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( executionId, trackId );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.STATIC_FIELD_SOURCEID, sourceId );

		dtd.insert( data );
	}

	public Map<String, Object> readNext( String executionId, String trackId, String source, int startTime,
			int interpolationLevel ) throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( executionId, trackId );

		Map<String, Object> data = new HashMap<String, Object>();
		data.put( DataTable.SELECT_ARG_TIMESTAMP_GTE, startTime );
		data.put( DataTable.SELECT_ARG_TIMESTAMP_LTE, System.currentTimeMillis() );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.SELECT_ARG_SOURCEID_EQ, sourceId );
		data.put( DataTable.SELECT_ARG_INTERPOLATIONLEVEL_EQ, interpolationLevel );

		return dtd.selectFirst( data );
	}

	public List<Map<String, Object>> read( String executionId, String trackId, String source, int startTime,
			int endTime, int interpolationLevel ) throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( executionId, trackId );

		Map<String, Object> data = new HashMap<String, Object>();
		data.put( DataTable.SELECT_ARG_TIMESTAMP_GTE, startTime );
		data.put( DataTable.SELECT_ARG_TIMESTAMP_LTE, endTime );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.SELECT_ARG_SOURCEID_EQ, sourceId );
		data.put( DataTable.SELECT_ARG_INTERPOLATIONLEVEL_EQ, interpolationLevel );

		return dtd.select( data );
	}

	public void deleteTrack( String executionId, String trackId ) throws SQLException
	{
		TableBase table = tableRegistry.getTable( executionId, trackId );
		if( table != null )
		{
			table.drop();

			// drop source table
			table = tableRegistry.getTable( executionId, trackId + SOURCE_TABLE_NAME_POSTFIX );
			if( table != null )
			{
				table.drop();
			}

			// dispose resources and remove from registry
			tableRegistry.dispose( executionId, trackId );
		}
	}

	public void delete( String executionId ) throws SQLException
	{
		List<TableBase> tableList = tableRegistry.getAllTables( executionId );
		TableBase t;
		for( int i = 0; i < tableList.size(); i++ )
		{
			t = tableList.get( i );
			t.drop();
		}
		for( int i = 0; i < tableList.size(); i++ )
		{
			tableRegistry.dispose( executionId, tableList.get( i ).getExternalName() );
		}
		// TODO delete from meta database, and remove from list of executions
		// TODO drop database?
	}

	public void dispose()
	{
		tableRegistry.dispose();
		connectionRegistry.dispose();
		ecs.removeAllExecutionListeners();
	}

	protected abstract void initializeDatabaseMetadata( DatabaseMetadata metadata );

	@Override
	public void pauseExecution()
	{
		// if started and not paused ( can not pause something that is not started
		// )
		if( executionState == State.STARTED )
		{
			executionState = State.PAUSED;
			ecs.fireExecutionPaused( State.STARTED );
			logger.debug( "State changed: START -> PAUSED" );
		}
	}

	@Override
	public void stopExecution()
	{
		// execution can be stopped only if started or paused previously
		if( executionState == State.STARTED || executionState == State.PAUSED )
		{
			State oldState = executionState;
			executionState = State.STOPPED;
			ecs.fireExecutionStopped( oldState );
			logger.debug( "State changed: " + oldState.name() + " -> STOPPED " );
		}
	}

	@Override
	public void removeAllExecutionListeners()
	{
		ecs.removeAllExecutionListeners();
	}

	@Override
	public void addExecutionListener( ExecutionListener el )
	{
		ecs.addExecutionListener( el );
	}

	@Override
	public void removeExecutionListener( ExecutionListener el )
	{
		ecs.removeExecutionListener( el );
	}

	@Override
	public State getState()
	{
		return executionState;
	}

}
