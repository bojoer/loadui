package com.eviware.loadui.impl.statistics.store.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DataTable extends TableBase
{
	public static final String STATIC_FIELD_TIMESTAMP = "_TSTAMP";
	public static final String STATIC_FIELD_SOURCEID = "_SOURCE_ID";

	public DataTable( String dbName, String name, String createTableExpr, String pkExpr,
			Map<String, ? extends Class<? extends Object>> dynamicFields,
			HashMap<Class<? extends Object>, String> typeConversionMap, Connection connection )
	{
		registerStaticField( STATIC_FIELD_TIMESTAMP, Integer.class );
		registerStaticField( STATIC_FIELD_SOURCEID, Integer.class );

		//addToPkSequence( STATIC_FIELD_TIMESTAMP );
		//addToPkSequence( STATIC_FIELD_SOURCEID );

		addSelectCriteria( STATIC_FIELD_TIMESTAMP, ">=?" );
		addSelectCriteria( STATIC_FIELD_TIMESTAMP, "<=?" );
		addSelectCriteria( STATIC_FIELD_SOURCEID, "=?" );

		init( dbName, name, createTableExpr, pkExpr, dynamicFields, typeConversionMap, connection );
	}

	@Override
	public void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		super.insert( data );
		//TODO commit here for now, maybe this will have to change
		commit();
	}

}
