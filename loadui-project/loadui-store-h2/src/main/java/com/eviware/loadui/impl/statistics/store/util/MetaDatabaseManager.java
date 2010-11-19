package com.eviware.loadui.impl.statistics.store.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public class MetaDatabaseManager
{
	public static final String METATABLE_NAME = "__meta_table";
	public static final String TIMESTAMP_COLUMN_NAME = "__tstamp";
	public static final String EXECUTION_COLUMN_NAME = "executionid";

	private DataSource dataSource;

	private Connection connection;
	private PreparedStatementHolder writeStatement;
	private PreparedStatement readAllStatement;
	private PreparedStatement readAllExecutionsStm;
	private PreparedStatement readExecutionStm;
	private PreparedStatement deleteAllStm;

	public MetaDatabaseManager( DataSource metabaseDataSource, String createTableExpr, String pkExpr,
			HashMap<Class<? extends Object>, String> typeConversionMap )
	{
		this.dataSource = metabaseDataSource;
		initMetaDatabase( createTableExpr, pkExpr, typeConversionMap );
	}

	private void initMetaDatabase( String createTableExpr, String pkExpr,
			HashMap<Class<? extends Object>, String> typeConversionMap )
	{
		Map<String, Class<? extends Object>> m = new HashMap<String, Class<? extends Object>>();
		m.put( EXECUTION_COLUMN_NAME, String.class );

		try
		{
			connection = dataSource.getConnection();
			String sql = SQLUtil.createTimestampTableCreateScript( createTableExpr, METATABLE_NAME, TIMESTAMP_COLUMN_NAME,
					Long.class, pkExpr, m, typeConversionMap );
			Statement stm = null;
			try
			{
				stm = connection.createStatement();
				stm.execute( sql );
			}
			catch( SQLException e )
			{
				// table already exist, do nothing
				// e.printStackTrace();
			}
			finally
			{
				JDBCUtil.close( stm );
			}

			StatementHolder inh = SQLUtil.createDataTableInsertScript( METATABLE_NAME, TIMESTAMP_COLUMN_NAME, m.keySet() );
			PreparedStatement writerPstm = connection.prepareStatement( inh.getStatementSql() );
			writeStatement = new PreparedStatementHolder( writerPstm, inh );
			readAllStatement = connection.prepareStatement( "select * from " + METATABLE_NAME );
			readAllExecutionsStm = connection.prepareStatement( "select " + EXECUTION_COLUMN_NAME + " from "
					+ METATABLE_NAME );
			readExecutionStm = connection.prepareStatement( "select * from " + METATABLE_NAME + " where "
					+ EXECUTION_COLUMN_NAME + " = ?" );
			deleteAllStm = connection.prepareStatement( "delete from " + METATABLE_NAME );
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Can't initialize meta database!", e );
		}
	}

	public void write( Number timestamp, Map<String, ? extends Object> data ) throws SQLException
	{
		writeStatement.setArguments( timestamp, data );
		writeStatement.executeUpdate();
	}

	public List<String> readExecutionNames() throws SQLException
	{
		List<String> result = new ArrayList<String>();
		ResultSet rs = readAllExecutionsStm.executeQuery();
		while( rs.next() )
		{
			result.add( rs.getString( 1 ) );
		}
		JDBCUtil.close( rs );
		return result;
	}

	public boolean executionExist( String executionId ) throws SQLException
	{
		readExecutionStm.setString( 1, executionId );
		ResultSet rs = readExecutionStm.executeQuery();
		boolean result = rs.next();
		JDBCUtil.close( rs );
		return result;
	}

	public Object[] readExecution( String executionId, String... columns ) throws SQLException
	{
		Object[] o = null;
		readExecutionStm.setString( 1, executionId );
		ResultSet rs = readExecutionStm.executeQuery();
		if( rs.next() )
		{
			o = new Object[columns.length];
			for( int i = 0; i < columns.length; i++ )
			{
				o[i] = rs.getObject( columns[i] );
			}
		}
		JDBCUtil.close( rs );
		return o;
	}

	public void clear() throws SQLException
	{
		deleteAllStm.executeUpdate();
	}

	public void commit() throws SQLException
	{
		connection.commit();
	}

	public void rollback() throws SQLException
	{
		connection.rollback();
	}

	public void dispose()
	{
		writeStatement.dispose();
		JDBCUtil.close( readAllStatement );
		JDBCUtil.close( readAllExecutionsStm );
		JDBCUtil.close( readExecutionStm );
		JDBCUtil.close( connection );
		JDBCUtil.close( deleteAllStm );
	}
}
