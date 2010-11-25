package com.eviware.loadui.impl.statistics.store;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.eviware.loadui.impl.statistics.store.table.ConnectionProvider;
import com.eviware.loadui.impl.statistics.store.table.TableBase;
import com.eviware.loadui.impl.statistics.store.table.model.MetaDatabaseMetaTable;
import com.eviware.loadui.impl.statistics.store.table.model.MetaTable;
import com.eviware.loadui.impl.statistics.store.table.model.SequenceTable;

public class ConnectionRegistry implements ConnectionProvider
{
	private DataSourceProvider dsProvider;

	private Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();

	private Map<String, Connection> connectionMap = new HashMap<String, Connection>();

	public ConnectionRegistry( DataSourceProvider dsProvider )
	{
		this.dsProvider = dsProvider;
	}

	public void addDataSource( String name, DataSource dataSource )
	{
		dataSourceMap.put( name, dataSource );
	}

	public DataSource getDataSource( String dbName ) throws SQLException
	{
		if( !dataSourceMap.containsKey( dbName ) )
		{
			dataSourceMap.put( dbName, dsProvider.createDataSource( dbName ) );
		}
		return dataSourceMap.get( dbName );
	}

	@Override
	public Connection getConnection( TableBase table ) throws SQLException
	{
		// for sequence and meta tables create separate connections. for data and
		// source tables use the same.
		DataSource dataSource = getDataSource( table.getDbName() );
		if( table instanceof SequenceTable || table instanceof MetaTable || table instanceof MetaDatabaseMetaTable )
		{
			String key = table.getDbName() + table.getTableName();
			if( !connectionMap.containsKey( key ) )
			{
				Connection conn = dataSource.getConnection();
				conn.setAutoCommit( false );
				connectionMap.put( key, conn );
			}
			return connectionMap.get( key );
		}
		else
		{
			String key = table.getDbName();
			if( !connectionMap.containsKey( key ) )
			{
				Connection conn = dataSource.getConnection();
				conn.setAutoCommit( false );
				connectionMap.put( key, conn );
			}
			return connectionMap.get( key );
		}
	}

}
