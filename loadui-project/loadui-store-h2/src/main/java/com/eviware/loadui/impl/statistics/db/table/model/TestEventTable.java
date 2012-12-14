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
package com.eviware.loadui.impl.statistics.db.table.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.TableDescriptor;

public class TestEventTable extends TableBase
{
	public static final String STATIC_FIELD_ID = "_ID";
	public static final String STATIC_FIELD_TIMESTAMP = "_TIMESTAMP";
	public static final String STATIC_FIELD_SOURCEID = "_SOURCEID";
	public static final String STATIC_FIELD_DATA = "_DATA";

	public static final String TABLE_NAME_PREFIX = "test_event";

	public static final String STATEMENT_GET_BY_OFFSET = "getByCountStatement";
	public static final String STATEMENT_GET_BY_TIME_RANGE = "getByTimeRangeStatement";
	public static final String STATEMENT_COUNT = "getCountStatement";

	public TestEventTable( String dbName, String name, ConnectionRegistry connectionRegistry,
			DatabaseMetadata databaseMetadata, TableRegistry tableRegistry ) throws SQLException
	{
		super( dbName, name, null, connectionRegistry, databaseMetadata, tableRegistry );
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_TIMESTAMP, Long.class );
		descriptor.addStaticField( STATIC_FIELD_SOURCEID, Long.class );
		descriptor.addStaticField( STATIC_FIELD_DATA, Byte[].class );

		descriptor.setAutoIncrementPK( STATIC_FIELD_ID );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return false;
	}

	@Override
	public synchronized void release()
	{
		super.release();
	}

	public synchronized List<Map<String, Object>> getByCount( List<Long> sources, int offset, int limit )
			throws SQLException
	{
		String query = "select * from " + getTableName() + " where " + STATIC_FIELD_SOURCEID + " in ("
				+ makeInStatement( sources.size() ) + ") order by " + STATIC_FIELD_TIMESTAMP + " limit ? offset ? ";

		List<Object> params = new ArrayList<>();
		params.addAll( sources );
		params.add( limit );
		params.add( offset );

		prepareStatement( STATEMENT_GET_BY_OFFSET, query );
		ResultSet rs = executeQuery( STATEMENT_GET_BY_OFFSET, params.toArray() );

		List<Map<String, Object>> result = new ArrayList<>();
		while( rs.next() )
		{
			Map<String, Object> rowMap = new HashMap<>();
			rowMap.put( STATIC_FIELD_ID, rs.getObject( STATIC_FIELD_ID ) );
			rowMap.put( STATIC_FIELD_SOURCEID, rs.getObject( STATIC_FIELD_SOURCEID ) );
			rowMap.put( STATIC_FIELD_DATA, rs.getBytes( STATIC_FIELD_DATA ) );
			rowMap.put( STATIC_FIELD_TIMESTAMP, rs.getObject( STATIC_FIELD_TIMESTAMP ) );
			result.add( rowMap );
		}
		return result;
	}

	public synchronized List<Map<String, Object>> getByTimeRange( List<Long> sources, long start, long end )
			throws SQLException
	{
		List<Object> params = new ArrayList<>();
		String query = "select * from " + getTableName() + " where " + STATIC_FIELD_SOURCEID + " in ("
				+ makeInStatement( sources.size() ) + ") and " + STATIC_FIELD_TIMESTAMP + " >= ? and "
				+ STATIC_FIELD_TIMESTAMP + " <= ? order by " + STATIC_FIELD_TIMESTAMP;
		params.addAll( sources );
		params.add( start );
		params.add( end );
		prepareStatement( STATEMENT_GET_BY_TIME_RANGE, query );

		ResultSet rs = executeQuery( STATEMENT_GET_BY_TIME_RANGE, params.toArray() );

		List<Map<String, Object>> result = new ArrayList<>();
		while( rs.next() )
		{
			Map<String, Object> rowMap = new HashMap<>();
			rowMap.put( STATIC_FIELD_ID, rs.getObject( STATIC_FIELD_ID ) );
			rowMap.put( STATIC_FIELD_SOURCEID, rs.getObject( STATIC_FIELD_SOURCEID ) );
			rowMap.put( STATIC_FIELD_DATA, rs.getBytes( STATIC_FIELD_DATA ) );
			rowMap.put( STATIC_FIELD_TIMESTAMP, rs.getObject( STATIC_FIELD_TIMESTAMP ) );
			result.add( rowMap );
		}
		return result;
	}

	public synchronized Integer getCount( List<Long> sources ) throws SQLException
	{
		String query = "select count(*) from " + getTableName() + " where " + STATIC_FIELD_SOURCEID + " in ("
				+ makeInStatement( sources.size() ) + ")";
		prepareStatement( STATEMENT_COUNT, query );

		ResultSet rs = executeQuery( STATEMENT_COUNT, sources.toArray() );
		if( rs.next() )
		{
			return rs.getInt( 1 );
		}
		else
		{
			return 0;
		}
	}

	private String makeInStatement( int size )
	{
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < size; i++ )
		{
			sb.append( "?" );
			if( i < size - 1 )
			{
				sb.append( ", " );
			}
		}
		return sb.toString();
	}
}
