package com.eviware.loadui.impl.statistics.store;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.impl.statistics.store.util.MetaDatabaseManager;
import com.eviware.loadui.impl.statistics.store.util.SQLUtil;

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
	private Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();

	private MetaDatabaseManager metaDatabaseManager;

	private ExecutionImpl currentExecution;

	private Map<String, Execution> executionMap = new HashMap<String, Execution>();

	private final Map<String, TrackDescriptor> trackDescriptors = new HashMap<String, TrackDescriptor>();

	public static ExecutionManager getInstance()
	{
		return instance;
	}

	public ExecutionManagerImpl()
	{
		instance = this;
		metaDatabaseManager = new MetaDatabaseManager( createDataSource( METADATABASE_NAME ), getCreateTableExpression(),
				getPrimaryKeyExpression(), getTypeConversionMap() );
	}

	@Override
	public Execution startExecution( String id, long timestamp )
	{
		// TODO Replace the following 2 lines with the commented out block of code
		// below (when it works).
		currentExecution = new ExecutionImpl( id, timestamp );
		return currentExecution;

		// try
		// {
		// if( metaDatabaseManager.executionExist( id ) )
		// {
		// throw new IllegalArgumentException(
		// "Execution with the specified id already exist!" );
		// }
		// currentExecution = new ExecutionImpl( id, timestamp );
		// executionMap.put( id, currentExecution );
		//
		// HashMap<String, Object> m = new HashMap<String, Object>();
		// m.put( MetaDatabaseManager.EXECUTION_COLUMN_NAME, id );
		// metaDatabaseManager.write( timestamp, m );
		//
		// return currentExecution;
		// }
		// catch( SQLException e )
		// {
		// throw new RuntimeException(
		// "Error while writing execution data to the database!", e );
		// }

	}

	/**
	 * Creates metadata table for the given execution if it does not exist.
	 * 
	 * @param e
	 *           Execution in which metadata table should be created
	 * @throws SQLException
	 */
	private void createMetaTable( Execution e ) throws SQLException
	{
		Connection conn = getConnection( e.getId() );

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
	public Track getTrack( String trackId )
	{
		// TODO create track in current execution, create table and meta table and
		// construct track instance

		if( currentExecution == null )
		{
			throw new IllegalArgumentException( "Current execution is null!" );
		}

		// return track if already exist, create new otherwise
		Track track = currentExecution.getTrack( trackId );
		if( track == null )
		{
			// create track instance
			// create corresponding table and metatable

			TrackDescriptor td = trackDescriptors.get( trackId );
			if( td == null )
			{
				throw new IllegalArgumentException( "No descriptor defined for specified trackId!" );
			}
			track = currentExecution.createTrack( trackId, td );

			if( 1 != 2 ) // Remove this once the code below works.
				return track;

			// create table if necessary

			String createSql = SQLUtil.createTimestampTableCreateScript( getCreateTableExpression(), trackId,
					TIMESTAMP_COLUMN_NAME, Integer.class, getPrimaryKeyExpression(), td.getValueNames(),
					getTypeConversionMap() );

			try
			{
				Connection connection = getConnection( currentExecution.getId() );
				Statement stm = connection.createStatement();
				ResultSet rs = stm.executeQuery( "select * from " + METATABLE_NAME + " where " );

			}
			catch( SQLException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		}
		catch( SQLException e )
		{
			// Do nothing. this is temporary method anyway
		}
	}

	protected abstract DataSource createDataSource( String db );

	protected abstract String getCreateTableExpression();

	protected abstract String getPrimaryKeyExpression();

	protected abstract HashMap<Class<? extends Object>, String> getTypeConversionMap();

}
