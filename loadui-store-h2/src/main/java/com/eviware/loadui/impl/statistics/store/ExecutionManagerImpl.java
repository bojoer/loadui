package com.eviware.loadui.impl.statistics.store;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.Execution;
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

public abstract class ExecutionManagerImpl implements ExecutionManager, DataSourceProvider
{

	private static final String METADATABASE_NAME = "__meta_database";

	private static final String SOURCE_TABLE_NAME_POSTFIX = "_sources";

	private MetaDatabaseMetaTable metaDatabaseMetaTable;

	private ExecutionImpl currentExecution;

	private Map<String, Execution> executionMap = new HashMap<String, Execution>();

	private final Map<String, TrackDescriptor> trackDescriptors = new HashMap<String, TrackDescriptor>();

	private final Map<String, Entry> latestEntries = new HashMap<String, Entry>();

	private TableRegistry tableRegistry = new TableRegistry();

	private DatabaseMetadata metadata;

	private ConnectionRegistry connectionRegistry;

	public ExecutionManagerImpl()
	{
		connectionRegistry = new ConnectionRegistry( this );

		metadata = new DatabaseMetadata();
		initializeDatabaseMetadata( metadata );

		metaDatabaseMetaTable = new MetaDatabaseMetaTable( METADATABASE_NAME, connectionRegistry, metadata, tableRegistry );
	}

	@Override
	public Execution startExecution( String id, long timestamp )
	{
		try
		{
			if( metaDatabaseMetaTable.exist( id ) )
			{
				throw new IllegalArgumentException( "Execution with the specified id already exist!" );
			}
			currentExecution = new ExecutionImpl( id, timestamp, this );
			executionMap.put( id, currentExecution );

			// create sequence table
			SequenceTable sequenceTable = new SequenceTable( id, connectionRegistry, metadata, tableRegistry );
			tableRegistry.put( id, sequenceTable );

			MetaTable metaTable = new MetaTable( id, connectionRegistry, metadata, tableRegistry );
			tableRegistry.put( id, metaTable );

			HashMap<String, Object> m = new HashMap<String, Object>();
			m.put( MetaDatabaseMetaTable.STATIC_FIELD_EXECUTION_NAME, id );
			m.put( MetaDatabaseMetaTable.STATIC_FIELD_TSTAMP, timestamp );
			metaDatabaseMetaTable.insert( m );

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
		if( track != null )
		{
			return track;
		}
		else
		{
			TrackDescriptor td = trackDescriptors.get( trackId );
			if( td == null )
			{
				throw new IllegalArgumentException( "No descriptor defined for specified trackId!" );
			}
			track = new TrackImpl( trackId, currentExecution, td, this );
			currentExecution.addTrack( track );
			try
			{
				String executionId = currentExecution.getId();

				// insert into meta-table
				Map<String, Object> data = new HashMap<String, Object>();
				data.put( MetaTable.STATIC_FIELD_TRACK_NAME, trackId );
				TableBase metaTable = tableRegistry.getTable( executionId, MetaTable.METATABLE_NAME );
				metaTable.insert( data );

				// create data table
				DataTable dtd = new DataTable( currentExecution.getId(), td.getId(), td.getValueNames(),
						connectionRegistry, metadata, tableRegistry );

				// create sources table
				SourceTable std = new SourceTable( currentExecution.getId(), td.getId() + SOURCE_TABLE_NAME_POSTFIX,
						connectionRegistry, metadata, tableRegistry );
				dtd.setParentTable( std );

				tableRegistry.put( executionId, dtd );
				tableRegistry.put( executionId, std );
			}
			catch( SQLException e )
			{
				throw new RuntimeException( "Unable to create track!", e );
			}
		}
		return track;
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
	}

	@Override
	public void unregisterTrackDescriptor( String trackId )
	{
		trackDescriptors.remove( trackId );
	}

	@Override
	public Collection<String> getTrackIds()
	{
		return Collections.unmodifiableSet( trackDescriptors.keySet() );
	}

	@Override
	public void writeEntry( String trackId, Entry entry, String source )
	{
		latestEntries.put( trackId + ":" + source, entry );

		Execution execution = getCurrentExecution();
		if( execution != null )
		{
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( DataTable.STATIC_FIELD_TIMESTAMP, entry.getTimestamp() );
			Collection<String> nameCollection = entry.getNames();
			for( Iterator<String> iterator = nameCollection.iterator(); iterator.hasNext(); )
			{
				String name = iterator.next();
				data.put( name, entry.getValue( name ) );
			}
			try
			{
				getTrack( trackId ); // Not sure if this is needed, but the Track
											// may not have been instantiated yet...
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
		return latestEntries.get( trackId + ":" + source );
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

	public void write( String executionId, String trackId, String source, Map<String, Object> data ) throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( executionId, trackId );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.STATIC_FIELD_SOURCEID, sourceId );

		dtd.insert( data );
	}

	public Map<String, Object> readNext( String executionId, String trackId, String source, int startTime )
			throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( executionId, trackId );

		Map<String, Object> data = new HashMap<String, Object>();
		data.put( DataTable.SELECT_ARG_TIMESTAMP_GTE, startTime );
		data.put( DataTable.SELECT_ARG_TIMESTAMP_LTE, System.currentTimeMillis() );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.SELECT_ARG_SOURCEID_EQ, sourceId );

		return dtd.selectFirst( data );
	}

	public List<Map<String, Object>> read( String executionId, String trackId, String source, int startTime, int endTime )
			throws SQLException
	{
		TableBase dtd = tableRegistry.getTable( executionId, trackId );

		Map<String, Object> data = new HashMap<String, Object>();
		data.put( DataTable.SELECT_ARG_TIMESTAMP_GTE, startTime );
		data.put( DataTable.SELECT_ARG_TIMESTAMP_LTE, endTime );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.SELECT_ARG_SOURCEID_EQ, sourceId );

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
	}

	protected abstract void initializeDatabaseMetadata( DatabaseMetadata metadata );

}
