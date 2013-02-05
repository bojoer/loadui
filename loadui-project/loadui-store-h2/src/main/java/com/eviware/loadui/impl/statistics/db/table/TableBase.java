/*
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.impl.statistics.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.util.JdbcUtil;

public abstract class TableBase implements Releasable
{
	public static final String TABLE_NAME_PREFIX = "_";

	private final String dbName;

	private final String externalName;

	private final String tableName;

	private final PreparedStatementHolder insertStatement;

	private final PreparedStatementHolder selectStatement;

	private final String createScript;

	private final String addPkIndexScript;

	private final PreparedStatement deleteStatement;

	private Map<String, ? extends Class<? extends Object>> dynamicFields = new HashMap<String, Class<? extends Object>>();

	private final Connection connection;

	private Map<String, PreparedStatement> extraStatementMap;

	private final TableDescriptor descriptor;

	private final TableRegistry tableRegistry;

	private final DatabaseMetadata databaseMetadata;

	public TableBase( String dbName, String name, Map<String, ? extends Class<? extends Object>> dynamicFields,
			ConnectionRegistry connectionRegistry, DatabaseMetadata databaseMetadata, TableRegistry tableRegistry )
			throws SQLException
	{
		descriptor = new TableDescriptor();
		initializeDescriptor( descriptor );

		this.externalName = name;
		this.tableName = buildTableName( name );
		this.dbName = dbName;
		this.dynamicFields = dynamicFields;
		this.tableRegistry = tableRegistry;
		this.databaseMetadata = databaseMetadata;

		this.connection = connectionRegistry.getConnection( this, useTableSpecificConnection() );

		addPkIndexScript = createAddPkIndexScript();
		createScript = createTableCreateScript();
		if( !exist() )
		{
			create();
		}

		StatementHolder sh = createTableInsertScript();
		insertStatement = new PreparedStatementHolder( connection.prepareStatement( sh.getStatementSql() ), sh );
		sh = createTableSelectScript();
		selectStatement = new PreparedStatementHolder( connection.prepareStatement( sh.getStatementSql() ), sh );
		deleteStatement = connection.prepareStatement( "DELETE FROM " + tableName );
	}

	final protected boolean exist()
	{
		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			stm.execute( "select * from " + tableName + " where 1 = 2" );
			return true;
		}
		catch( SQLException e )
		{
			return false;
		}
		finally
		{
			if( stm != null )
			{
				JdbcUtil.close( stm );
			}
		}
	}

	protected final StatementHolder createTableSelectScript()
	{
		List<String[]> selectCriteria = descriptor.getSelectCriteria();
		StatementHolder sh = new StatementHolder();
		StringBuilder crieriaParams = new StringBuilder();
		for( int i = 0; i < selectCriteria.size(); i++ )
		{
			crieriaParams.append( selectCriteria.get( i )[1] + " " + selectCriteria.get( i )[2] );

			if( i < selectCriteria.size() - 1 )
			{
				crieriaParams.append( " AND " );
			}
			sh.addArgument( selectCriteria.get( i )[0] );
		}
		if( crieriaParams.length() > 0 )
		{
			crieriaParams.insert( 0, " WHERE " );
		}

		sh.setStatementSql( "SELECT * FROM " + tableName + crieriaParams.toString() );
		return sh;
	}

	final protected StatementHolder createTableInsertScript()
	{
		StatementHolder sh = new StatementHolder();
		String key;
		StringBuilder names = new StringBuilder();
		StringBuilder values = new StringBuilder();

		Iterator<String> keys = descriptor.getStaticFields().keySet().iterator();
		while( keys.hasNext() )
		{
			key = keys.next();
			names.append( key );
			values.append( "?" );
			if( keys.hasNext() )
			{
				names.append( ", " );
				values.append( ", " );
			}
			sh.addArgument( key );
		}
		if( dynamicFields != null && dynamicFields.size() > 0 )
		{
			names.append( ", " );
			values.append( ", " );
			keys = dynamicFields.keySet().iterator();
			while( keys.hasNext() )
			{
				key = keys.next();
				names.append( key );
				values.append( "?" );
				if( keys.hasNext() )
				{
					names.append( ", " );
					values.append( ", " );
				}
				sh.addArgument( key );
			}
		}
		sh.setStatementSql( "INSERT INTO " + tableName + "( " + names.toString() + " ) VALUES ( " + values.toString()
				+ " )" );
		return sh;
	}

	final protected String createTableCreateScript()
	{
		HashMap<Class<? extends Object>, String> typeConversionMap = databaseMetadata.getTypeConversionMap();

		Map<String, ? extends Object> staticFields = descriptor.getStaticFields();
		List<String> pkSequence = descriptor.getPkSequence();

		StringBuilder b = new StringBuilder();
		b.append( databaseMetadata.getCreateTableExpression() );
		b.append( " " );
		b.append( tableName );
		b.append( " ( " );

		if( descriptor.getAutoIncrementPK() != null )
		{
			b.append( descriptor.getAutoIncrementPK() );
			b.append( " " );
			b.append( databaseMetadata.getAutoIncrementPKExpression() );
			if( staticFields.size() > 0 || dynamicFields != null && dynamicFields.size() > 0 )
			{
				b.append( ", " );
			}
		}

		String key;
		Iterator<String> keys = staticFields.keySet().iterator();
		while( keys.hasNext() )
		{
			key = keys.next();
			b.append( key );
			b.append( " " );
			b.append( typeConversionMap.get( staticFields.get( key ) ) );
			if( pkSequence.contains( key ) )
			{
				b.append( " NOT NULL " );
			}
			if( keys.hasNext() )
			{
				b.append( ", " );
			}
		}
		if( dynamicFields != null && dynamicFields.size() > 0 )
		{
			b.append( ", " );
			keys = dynamicFields.keySet().iterator();
			while( keys.hasNext() )
			{
				key = keys.next();
				b.append( key );
				b.append( " " );
				b.append( typeConversionMap.get( dynamicFields.get( key ) ) );
				if( keys.hasNext() )
				{
					b.append( ", " );
				}
			}
		}
		b.append( " ) " );
		return b.toString();
	}

	final protected String createAddPkIndexScript()
	{
		List<String> pkSequence = descriptor.getPkSequence();
		if( pkSequence.size() == 0 )
		{
			return null;
		}
		StringBuilder b = new StringBuilder();
		for( int i = 0; i < pkSequence.size(); i++ )
		{
			b.append( pkSequence.get( i ) );
			if( i < pkSequence.size() - 1 )
			{
				b.append( ", " );
			}
		}
		return databaseMetadata.getAddPrimaryKeyIndexExpression().replaceFirst( "\\?", tableName )
				.replaceFirst( "\\?", tableName ).replaceFirst( "\\?", b.toString() );
	}

	public synchronized void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		insertStatement.setArguments( data );
		insertStatement.executeUpdate();
		commit();
	}

	private void create() throws SQLException
	{
		Statement stm = connection.createStatement();
		try
		{
			stm.execute( createScript );
			if( descriptor.getAutoIncrementPK() == null && addPkIndexScript != null )
			{
				stm.execute( addPkIndexScript );
			}
		}
		finally
		{
			JdbcUtil.close( stm );
		}
	}

	public synchronized List<Map<String, Object>> select( Map<String, Object> data ) throws SQLException
	{
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		selectStatement.setArguments( data );
		ResultSet rs = selectStatement.executeQuery();
		Map<String, Object> row;
		while( rs.next() )
		{
			row = new HashMap<String, Object>();
			for( int i = 0; i < rs.getMetaData().getColumnCount(); i++ )
			{
				int columnType = rs.getMetaData().getColumnType( i + 1 );
				if( columnType == Types.BLOB || columnType == Types.CLOB )
				{
					row.put( rs.getMetaData().getColumnName( i + 1 ), rs.getBytes( i + 1 ) );
				}
				else
				{
					row.put( rs.getMetaData().getColumnName( i + 1 ), rs.getObject( i + 1 ) );
				}
			}
			result.add( row );
		}
		JdbcUtil.close( rs );
		return result;
	}

	public synchronized Map<String, Object> selectFirst( Map<String, Object> data ) throws SQLException
	{
		selectStatement.setArguments( data );
		ResultSet rs = selectStatement.executeQuery();
		Map<String, Object> row = new HashMap<String, Object>();
		if( rs.next() )
		{
			for( int i = 0; i < rs.getMetaData().getColumnCount(); i++ )
			{
				int columnType = rs.getMetaData().getColumnType( i + 1 );
				if( columnType == Types.BLOB || columnType == Types.CLOB )
				{
					row.put( rs.getMetaData().getColumnName( i + 1 ), rs.getBytes( i + 1 ) );
				}
				else
				{
					row.put( rs.getMetaData().getColumnName( i + 1 ), rs.getObject( i + 1 ) );
				}
			}
		}
		JdbcUtil.close( rs );
		return row;
	}

	public synchronized void delete() throws SQLException
	{
		deleteStatement.execute();
		commit();
	}

	public synchronized void drop() throws SQLException
	{
		Statement stm = connection.createStatement();
		try
		{
			stm.execute( "drop table " + tableName );
		}
		finally
		{
			stm.close();
		}
	}

	@Override
	public synchronized void release()
	{
		selectStatement.release();
		insertStatement.release();
		JdbcUtil.close( deleteStatement );
		if( extraStatementMap != null )
		{
			Iterator<Entry<String, PreparedStatement>> iterator = extraStatementMap.entrySet().iterator();
			while( iterator.hasNext() )
			{
				JdbcUtil.close( iterator.next().getValue() );
			}
		}
		// only table resources are released here (e.g. statements). connection is
		// not closed here, since it may be shared between tables.
		// ConnectionRegistry handles connections and data sources disposal
		// instead.
	}

	public String getExternalName()
	{
		return externalName;
	}

	public String getTableName()
	{
		return tableName;
	}

	public String getDbName()
	{
		return dbName;
	}

	protected void execute( String statement, Object[] params ) throws SQLException
	{
		PreparedStatement stm = extraStatementMap.get( statement );
		if( stm != null )
		{
			if( params != null )
			{
				for( int i = 0; i < params.length; i++ )
				{
					stm.setObject( i + 1, params[i] );
				}
			}
			stm.execute();
			commit();
		}
	}

	protected ResultSet executeQuery( String statement, Object[] params ) throws SQLException
	{
		PreparedStatement stm = extraStatementMap.get( statement );
		if( stm != null )
		{
			if( params != null && params.length > 0 )
			{
				for( int i = 0; i < params.length; i++ )
				{
					stm.setObject( i + 1, params[i] );
				}
			}
			return stm.executeQuery();
		}
		return null;
	}

	protected void prepareStatement( String name, String sql )
	{
		try
		{
			if( extraStatementMap == null )
			{
				extraStatementMap = new HashMap<String, PreparedStatement>();
			}
			extraStatementMap.put( name, connection.prepareStatement( sql ) );
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to initialize extra statement!", e );
		}
	}

	protected void commit() throws SQLException
	{
		connection.commit();
	}

	final protected String buildTableName( String name )
	{
		return TABLE_NAME_PREFIX + name;
	}

	public TableBase getTable( String externalName )
	{
		return tableRegistry.getTable( dbName, externalName );
	}

	/**
	 * Concrete table implementation must implement this to specify static
	 * fields, primary key sequence, select criteria parameters
	 */
	protected abstract void initializeDescriptor( TableDescriptor descriptor );

	/**
	 * Must be implemented by concrete table implementations. Determines if that
	 * table type will use its own connection for data handling. If true, new
	 * database connection will be created and it will be used just by that table
	 * type. If false, common connection will be used.
	 */
	protected abstract boolean useTableSpecificConnection();
}
