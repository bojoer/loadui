package com.eviware.loadui.impl.statistics.store;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.impl.statistics.store.util.MetaDatabaseManager;

public abstract class ExecutionManagerImpl implements ExecutionManager
{

	private static final String METADATABASE_NAME = "__meta_database";
	private static final String METATABLE_NAME = "__meta_table";
	private static final String TIMESTAMP_COLUMN_NAME = "__tstamp";

	private static ExecutionManager instance;

	/**
	 * Data sources used to communicate with database. This map contains data
	 * sources for different database instances, and concrete implementation of
	 * it depends on database that is used. Some implementations may provide
	 * connection pool, some may not. Map key is the id of the execution
	 * instance, and value is data source used to establish connection.
	 */
	private HashMap<String, DataSource> dataSourceMap;

	private MetaDatabaseManager metabase;

	private Execution currentExecution;

	private Map<String, Execution> executionMap = new HashMap<String, Execution>();

	public static ExecutionManager getInstance()
	{
		return instance;
	}

	public ExecutionManagerImpl()
	{
		instance = this;
		metabase = new MetaDatabaseManager( createDataSource( METADATABASE_NAME ), getCreateTableExpression(),
				getPrimaryKeyExpression(), getTypeConversionMap() );
	}

	@Override
	public Execution startExecution( String id, long timestamp )
	{
		try
		{
			if( metabase.executionExist( id ) )
			{
				throw new IllegalArgumentException( "Execution with the specified id already exist!" );
			}
			Execution execution = new ExecutionImpl( id, timestamp );
			executionMap.put( id, execution );
			currentExecution = execution;

			HashMap<String, Object> m = new HashMap<String, Object>();
			m.put( MetaDatabaseManager.EXECUTION_COLUMN_NAME, id );
			metabase.write( timestamp, m );
			return execution;
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
		return dataSourceMap.get( executionId ).getConnection();
	}

	@Override
	public Execution getCurrentExecution()
	{
		return currentExecution;
	}

	@Override
	public Track createTrack( String trackId, Map<String, Class<? extends Number>> trackStructure )
	{
		// TODO create track in current execution, create table and meta table and
		// construct track instance
		return null;
	}

	@Override
	public Collection<String> getExecutionNames()
	{
		try
		{
			return metabase.readExecutionNames();
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
				Object[] o = metabase.readExecution( executionId, MetaDatabaseManager.TIMESTAMP_COLUMN_NAME,
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

	public void clearMetaDatabase()
	{
		try
		{
			metabase.clear();
		}
		catch( SQLException e )
		{
			// Do nothing. this is temporary methid anyway
		}
	}

	protected abstract DataSource createDataSource( String db );

	protected abstract String getCreateTableExpression();

	protected abstract String getPrimaryKeyExpression();

	protected abstract HashMap<Class<? extends Object>, String> getTypeConversionMap();

}
