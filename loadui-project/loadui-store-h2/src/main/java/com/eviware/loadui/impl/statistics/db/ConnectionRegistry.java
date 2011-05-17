/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.impl.statistics.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.DataSource;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.util.JdbcUtil;

public class ConnectionRegistry implements Releasable
{
	private DataSourceProvider dsProvider;

	private HashMap<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();

	private HashMap<String, Connection> connectionMap = new HashMap<String, Connection>();

	public ConnectionRegistry( DataSourceProvider dsProvider )
	{
		this.dsProvider = dsProvider;
	}

	/**
	 * Retrieves data source (creates it if necessary) for a specified database
	 * 
	 * @param dbName
	 * @return Data source for a specified database
	 * @throws SQLException
	 */
	public DataSource getDataSource( String dbName ) throws SQLException
	{
		if( !dataSourceMap.containsKey( dbName ) )
		{
			dataSourceMap.put( dbName, dsProvider.createDataSource( dbName ) );
		}
		return dataSourceMap.get( dbName );
	}

	/**
	 * Releases all connections and data sources for a specified database
	 */
	public void release( String dbName )
	{
		// release all connections to the database
		ArrayList<String> connectionsToClose = new ArrayList<String>();
		Iterator<String> keys = connectionMap.keySet().iterator();
		while( keys.hasNext() )
		{
			String key = keys.next();
			if( key.startsWith( dbName ) )
			{
				connectionsToClose.add( key );
			}
		}
		for( int i = 0; i < connectionsToClose.size(); i++ )
		{
			// remove connection from connection map and close it
			JdbcUtil.close( connectionMap.remove( connectionsToClose.get( i ) ) );
		}

		// release database data source
		DataSource ds = dataSourceMap.remove( dbName );
		if( ds != null )
		{
			dsProvider.releaseDataSource( ds );
		}
	}

	/**
	 * Releases all connections and data sources
	 */
	@Override
	public void release()
	{
		// close all connections
		Collection<Connection> connections = connectionMap.values();
		for( Connection c : connections )
		{
			JdbcUtil.close( c );
		}
		connectionMap.clear();

		// release all data sources
		Collection<DataSource> dataSources = dataSourceMap.values();
		for( DataSource ds : dataSources )
		{
			dsProvider.releaseDataSource( ds );
		}
		dataSourceMap.clear();
	}

	public Connection getConnection( TableBase table, boolean tableSpecific ) throws SQLException
	{
		if( tableSpecific )
		{
			return getOrCreateConnection( table.getDbName(), table.getDbName() + table.getTableName() );
		}
		else
		{
			return getConnection( table.getDbName() );
		}
	}

	public Connection getConnection( String dbName ) throws SQLException
	{
		return getOrCreateConnection( dbName, dbName );
	}

	private Connection getOrCreateConnection( String dbName, String key ) throws SQLException
	{
		
		DataSource dataSource = getDataSource( dbName );
		if( !connectionMap.containsKey( key ) )
		{
			Connection conn = dataSource.getConnection();
			conn.setAutoCommit( false );
			connectionMap.put( key, conn );
		}
		return connectionMap.get( key );
	}

}
