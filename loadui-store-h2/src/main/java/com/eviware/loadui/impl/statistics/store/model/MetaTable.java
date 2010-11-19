package com.eviware.loadui.impl.statistics.store.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MetaTable extends TableBase
{
	public static final String STATIC_FIELD_TRACK_NAME = "__track_id";

	public MetaTable( String dbName, String name, String createTableExpr, String pkExpr,
			Map<String, ? extends Class<? extends Object>> dynamicFields,
			HashMap<Class<? extends Object>, String> typeConversionMap, Connection connection )
	{
		registerStaticField( STATIC_FIELD_TRACK_NAME, String.class );
		addToPkSequence( STATIC_FIELD_TRACK_NAME );
		addSelectCriteria( STATIC_FIELD_TRACK_NAME, "=?" );
		init( dbName, name, createTableExpr, pkExpr, dynamicFields, typeConversionMap, connection );
	}

	@Override
	public void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		Map<String, Object> queryData = new HashMap<String, Object>();
		queryData.put( STATIC_FIELD_TRACK_NAME, data.get( STATIC_FIELD_TRACK_NAME ) );
		if( select( queryData ).size() == 0 )
		{
			super.insert( data );
		}
	}
}
