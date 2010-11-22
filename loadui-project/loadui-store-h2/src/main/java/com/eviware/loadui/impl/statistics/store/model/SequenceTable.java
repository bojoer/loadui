package com.eviware.loadui.impl.statistics.store.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequenceTable extends TableBase
{
	private static Map<String, SequenceTable> instanceMap = new HashMap<String, SequenceTable>();
	
	public static SequenceTable getInstance(String dbName){
		return instanceMap.get( dbName );
	}
	
	public static final String STATIC_FIELD_TABLE = "__TABLE";
	public static final String STATIC_FIELD_COLUMN = "__COLUMN";
	public static final String STATIC_FIELD_VALUE = "__VALUE";
	
	public static final String STATEMENT_UPDATE_VALUE = "updateValueStatement";

	public SequenceTable( String dbName, String name, String createTableExpr, String pkExpr,
			Map<String, ? extends Class<? extends Object>> dynamicFields,
			HashMap<Class<? extends Object>, String> typeConversionMap, Connection connection )
	{
		registerStaticField( STATIC_FIELD_TABLE, String.class );
		registerStaticField( STATIC_FIELD_COLUMN, String.class );
		registerStaticField( STATIC_FIELD_VALUE, Integer.class );

		addToPkSequence( STATIC_FIELD_TABLE );
		addToPkSequence( STATIC_FIELD_COLUMN );

		addSelectCriteria( STATIC_FIELD_TABLE, "=?" );
		addSelectCriteria( STATIC_FIELD_COLUMN, "=?" );

		init( dbName, name, createTableExpr, pkExpr, dynamicFields, typeConversionMap, connection );

		prepareStatement( STATEMENT_UPDATE_VALUE, "update " + getTableName() + " set " + STATIC_FIELD_VALUE + " = ? where "
				+ STATIC_FIELD_TABLE + " = ? and " + STATIC_FIELD_COLUMN + " = ?" );

		instanceMap.put( dbName, this );
	}

	public synchronized Integer next( String tableName, String column )
	{
		try
		{
			Integer id;
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( STATIC_FIELD_TABLE, tableName );
			data.put( STATIC_FIELD_COLUMN, column );
			List<Map<String, ? extends Object>> rs = select( data );
			if( rs.size() == 0 )
			{
				id = 0;
				data.clear();
				data.put( STATIC_FIELD_TABLE, tableName );
				data.put( STATIC_FIELD_COLUMN, column );
				data.put( STATIC_FIELD_VALUE, id );
				insert( data );
			}
			else
			{
				id = ( Integer )rs.get( 0 ).get( STATIC_FIELD_VALUE ) + 1;
				Object[] params = new Object[3];
				params[0] = id;
				params[1] = tableName;
				params[2] = column;
				execute( STATEMENT_UPDATE_VALUE, params );
			}
			commit();
			return id;
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to retrieve next sequence value!", e );
		}
	}

}
