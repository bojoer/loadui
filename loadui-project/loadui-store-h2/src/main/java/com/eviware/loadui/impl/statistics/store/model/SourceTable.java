package com.eviware.loadui.impl.statistics.store.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceTable extends TableBase
{
	public static final String STATIC_FIELD_SOURCE_NAME = "__SOURCE";
	public static final String STATIC_FIELD_SOURCEID = "__SOURCE_ID";

	private Map<String, Integer> inMemoryTable = new HashMap<String, Integer>();

	public SourceTable( String dbName, String name, String createTableExpr, String pkExpr,
			Map<String, ? extends Class<? extends Object>> dynamicFields,
			HashMap<Class<? extends Object>, String> typeConversionMap, Connection connection )
	{
		registerStaticField( STATIC_FIELD_SOURCE_NAME, String.class );
		registerStaticField( STATIC_FIELD_SOURCEID, Integer.class );

		addToPkSequence( STATIC_FIELD_SOURCE_NAME );

		init( dbName, name, createTableExpr, pkExpr, dynamicFields, typeConversionMap, connection );

		try
		{
			List<Map<String, ? extends Object>> result = select( null );
			for( int i = 0; i < result.size(); i++ )
			{
				inMemoryTable.put( ( String )result.get( i ).get( STATIC_FIELD_SOURCE_NAME ), ( Integer )result.get( i ).get( STATIC_FIELD_SOURCEID ) );
			}
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to read data from table!", e );
		}
	}

	@Override
	public void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		super.insert( data );
		//TODO commit for now.
		commit();
		inMemoryTable.put( ( String )data.get( STATIC_FIELD_SOURCE_NAME ), ( Integer )data.get( STATIC_FIELD_SOURCEID ) );
	}

	public synchronized Integer getSourceId( String source )
	{
		if( inMemoryTable.get( source ) == null )
		{
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( STATIC_FIELD_SOURCE_NAME, source );
			data.put( STATIC_FIELD_SOURCEID,
					SequenceTable.getInstance( getDbName() ).next( getExternalName(), STATIC_FIELD_SOURCEID ) );
			try
			{
				insert( data );
				commit();
			}
			catch( SQLException e )
			{
				throw new RuntimeException( "Unable to retrieve sourceid value!", e );
			}
		}
		return inMemoryTable.get( source );
	}

}
