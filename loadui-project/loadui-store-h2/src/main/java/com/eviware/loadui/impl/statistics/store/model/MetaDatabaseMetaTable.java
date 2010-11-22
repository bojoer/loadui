package com.eviware.loadui.impl.statistics.store.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.impl.statistics.store.util.JDBCUtil;

public class MetaDatabaseMetaTable extends TableBase
{
	public static final String STATIC_FIELD_TSTAMP = "_TSTAMP";
	public static final String STATIC_FIELD_EXECUTION_NAME = "_EXECUTION";

	public static final String STATEMENT_LIST_EXECUTIONS = "LIST_EXECUTIONS";

	public MetaDatabaseMetaTable( String dbName, String name, String createTableExpr, String pkExpr,
			Map<String, ? extends Class<? extends Object>> dynamicFields,
			HashMap<Class<? extends Object>, String> typeConversionMap, Connection connection )
	{
		registerStaticField( STATIC_FIELD_TSTAMP, Long.class );
		registerStaticField( STATIC_FIELD_EXECUTION_NAME, String.class );

		addToPkSequence( STATIC_FIELD_EXECUTION_NAME );

		addSelectCriteria( STATIC_FIELD_EXECUTION_NAME, "=?" );
		init( dbName, name, createTableExpr, pkExpr, dynamicFields, typeConversionMap, connection );

		prepareStatement( STATEMENT_LIST_EXECUTIONS, "select " + STATIC_FIELD_EXECUTION_NAME + " from " + getTableName() );
	}

	@Override
	public void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		super.insert( data );
		//TODO commit for now.
		commit();
	}
	
	public boolean exist( String executionId ) throws SQLException
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put( STATIC_FIELD_EXECUTION_NAME, executionId );
		List<Map<String, ? extends Object>> result = select( data );
		return result.size() > 0;
	}

	public List<String> list() throws SQLException
	{
		List<String> result = new ArrayList<String>();
		ResultSet rs = executeQuery( STATEMENT_LIST_EXECUTIONS, null );
		if( rs != null )
		{
			while( rs.next() )
			{
				result.add( rs.getString( STATIC_FIELD_EXECUTION_NAME ) );
			}
		}
		JDBCUtil.close( rs );
		return result;
	}
}
