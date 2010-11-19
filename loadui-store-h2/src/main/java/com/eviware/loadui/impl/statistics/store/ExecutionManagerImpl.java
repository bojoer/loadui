package com.eviware.loadui.impl.statistics.store;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.impl.statistics.store.model.DataTable;
import com.eviware.loadui.impl.statistics.store.model.MetaTable;
import com.eviware.loadui.impl.statistics.store.model.SequenceTable;
import com.eviware.loadui.impl.statistics.store.model.SourceTable;
import com.eviware.loadui.impl.statistics.store.model.TableBase;
import com.eviware.loadui.impl.statistics.store.model.TableRegistry;
import com.eviware.loadui.impl.statistics.store.util.MetaDatabaseManager;

public abstract class ExecutionManagerImpl implements ExecutionManager
{

	private static final String METADATABASE_NAME = "__meta_database";
	private static final String METATABLE_NAME = "meta_table";
	private static final String SEQUENCE_TABLE_NAME = "sequence_table";
	private static final String SOURCES_TABLE_NAME_POSTFIX = "_sources";

	private static ExecutionManager instance;

	/**
	 * Data sources used to communicate with database. This map contains data
	 * sources for different database instances, and concrete implementation of
	 * it depends on database that is used. Some implementations may provide
	 * connection pool, some may not. Map key is the id of the execution
	 * instance, and value is data source used to establish connection.
	 */
	private Map<String, DataSource> dataSourceMap;

	/**
	 * Every execution uses one connection for all operations.
	 * References to these are kept in this map.
	 */
	private Map<String, Connection> connectionMap = new HashMap<String, Connection>();;

	private MetaDatabaseManager metaDatabaseManager;

	private ExecutionImpl currentExecution;

	private Map<String, Execution> executionMap = new HashMap<String, Execution>();

	private final Map<String, TrackDescriptor> trackDescriptors = new HashMap<String, TrackDescriptor>();

	private TableRegistry tableRegistry = new TableRegistry();

	public static ExecutionManager getInstance()
	{
		return instance;
	}

	public ExecutionManagerImpl()
	{
		instance = this;
		metaDatabaseManager = new MetaDatabaseManager( createDataSource( METADATABASE_NAME ), getCreateTableExpression(),
				getPrimaryKeyExpression(), getTypeConversionMap() );
		dataSourceMap = new HashMap<String, DataSource>();
	}

	@Override
	public Execution startExecution( String id, long timestamp )
	{
		try
		{
			if( metaDatabaseManager.executionExist( id ) )
			{
				throw new IllegalArgumentException( "Execution with the specified id already exist!" );
			}
			currentExecution = new ExecutionImpl( id, timestamp );
			executionMap.put( id, currentExecution );

			Connection connection = getConnection( id );

			// create sequence table
			new SequenceTable( id, SEQUENCE_TABLE_NAME, getCreateTableExpression(), getAddPrimaryKeyIndexExpression(),
					null, getTypeConversionMap(), connection );

			// TODO commit in finally block, after meta table was created and data
			// was written to the database.
			// create table can not be rolled back, so do what? if writing to the
			// meta database fail, drop created meta table
			// and if meta table creation fails roll back don't write to the meta
			// database at all.

			MetaTable metaTable = new MetaTable( id, METATABLE_NAME, getCreateTableExpression(),
					getAddPrimaryKeyIndexExpression(), null, getTypeConversionMap(), connection );
			tableRegistry.putMetaTable( id, metaTable );

			HashMap<String, Object> m = new HashMap<String, Object>();
			m.put( MetaDatabaseManager.EXECUTION_COLUMN_NAME, id );
			metaDatabaseManager.write( timestamp, m );

			return currentExecution;
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Error while writing execution data to the database!", e );
		}
	}

	public Connection getConnection( String executionId ) throws SQLException
	{
		if( !dataSourceMap.containsKey( executionId ) )
		{
			dataSourceMap.put( executionId, createDataSource( executionId ) );
		}
		if( !connectionMap.containsKey( executionId ) )
		{
			Connection conn = dataSourceMap.get( executionId ).getConnection();
			conn.setAutoCommit( false );
			connectionMap.put( executionId, conn );
		}
		return connectionMap.get( executionId );
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
			track = currentExecution.createTrack( trackId, td );
			try
			{
				String executionId = currentExecution.getId();

				// insert into meta-table
				Map<String, Object> data = new HashMap<String, Object>();
				data.put( MetaTable.STATIC_FIELD_TRACK_NAME, trackId );
				TableBase metaTable = tableRegistry.getMetaTable( executionId );
				metaTable.insert( data );

				// create actual tables
				Connection connection = getConnection( currentExecution.getId() );

				// create data table
				DataTable dtd = new DataTable( currentExecution.getId(), td.getId(), getCreateTableExpression(),
						getAddPrimaryKeyIndexExpression(), td.getValueNames(), getTypeConversionMap(), connection );

				// create sources table
				SourceTable std = new SourceTable( currentExecution.getId(), td.getId() + SOURCES_TABLE_NAME_POSTFIX,
						getCreateTableExpression(), getAddPrimaryKeyIndexExpression(), null, getTypeConversionMap(),
						connection );
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
			return metaDatabaseManager.readExecutionNames();
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
				Object[] o = metaDatabaseManager.readExecution( executionId, MetaDatabaseManager.TIMESTAMP_COLUMN_NAME,
						MetaDatabaseManager.EXECUTION_COLUMN_NAME );
				if( o == null )
				{
					throw new IllegalArgumentException( "Execution with the specified id does not exist!" );
				}
				else
				{
					return new ExecutionImpl( ( String )o[1], ( Long )o[0] );
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

	public void clearMetaDatabase()
	{
		try
		{
			metaDatabaseManager.clear();
			metaDatabaseManager.commit();
		}
		catch( SQLException e )
		{
			// Do nothing. this is temporary method anyway
		}
	}

	public void write( String executionId, String trackId, String source, Map<String, Object> data ) throws SQLException
	{
		TableBase dtd = tableRegistry.get( executionId, trackId );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.STATIC_FIELD_SOURCEID, sourceId );

		dtd.insert( data );
	}

	public List<Map<String, ? extends Object>> read( String executionId, String trackId, String source, int startTime,
			int endTime ) throws SQLException
	{
		TableBase dtd = tableRegistry.get( executionId, trackId );

		Map<String, Object> data = new HashMap<String, Object>();
		data.put( DataTable.STATIC_FIELD_TIMESTAMP, startTime );
		data.put( DataTable.STATIC_FIELD_TIMESTAMP, endTime );

		Integer sourceId = ( ( SourceTable )dtd.getParentTable() ).getSourceId( source );
		data.put( DataTable.STATIC_FIELD_SOURCEID, sourceId );

		return dtd.select( data );
	}

	public void deleteTrack( String executionId, String trackId ) throws SQLException
	{
		TableBase table = tableRegistry.get( executionId, trackId );
		if( table != null )
		{
			table.delete();
			table = tableRegistry.get( executionId, trackId + SOURCES_TABLE_NAME_POSTFIX );
			if( table != null )
			{
				table.delete();
			}
		}
	}

	public void delete( String executionId ) throws SQLException
	{
		List<TableBase> tableList = tableRegistry.get( executionId );
		for( int i = 0; i < tableList.size(); i++ )
		{
			tableList.get( i ).delete();
		}
	}

	public void dispose()
	{
		tableRegistry.dispose();
	}

	protected abstract DataSource createDataSource( String db );

	protected abstract String getCreateTableExpression();

	protected abstract String getPrimaryKeyExpression();

	protected abstract HashMap<Class<? extends Object>, String> getTypeConversionMap();

	protected abstract String getAddPrimaryKeyIndexExpression();
}
