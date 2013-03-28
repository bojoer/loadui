/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.statistics.db.table.model;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.TableDescriptor;
import com.google.common.collect.ImmutableMap;

public class TestEventTypeTable extends TableBase
{
	public static final String STATIC_FIELD_ID = "_ID";
	public static final String STATIC_FIELD_LABEL = "_LABEL";
	public static final String STATIC_FIELD_TYPE = "_TYPE";

	public static final String TABLE_NAME = "test_event_type";

	private Map<String, Map<String, Object>> inMemoryTable = new HashMap<>();

	public TestEventTypeTable( String dbName, ConnectionRegistry connectionRegistry, DatabaseMetadata databaseMetadata,
			TableRegistry tableRegistry ) throws SQLException
	{
		super( dbName, TABLE_NAME, null, connectionRegistry, databaseMetadata, tableRegistry );
		loadInMemoryTable();
	}

	@Override
	public synchronized void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		super.insert( data );
		loadInMemoryTable();
	}

	@Override
	public synchronized void release()
	{
		super.release();
		inMemoryTable.clear();
		inMemoryTable = null;
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_LABEL, String.class );
		descriptor.addStaticField( STATIC_FIELD_TYPE, String.class );

		descriptor.setAutoIncrementPK( STATIC_FIELD_ID );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return true;
	}

	public synchronized Map<String, Map<String, Object>> getInMemoryTable()
	{
		return ImmutableMap.copyOf( inMemoryTable );
	}

	public synchronized String getTypeNameById( Long id )
	{
		for( Entry<String, Map<String, Object>> e : inMemoryTable.entrySet() )
		{
			if( id.equals( e.getValue().get( STATIC_FIELD_ID ) ) )
			{
				return e.getKey();
			}
		}
		return null;
	}

	public synchronized Long getIdByTypeName( String typeName )
	{
		return ( Long )inMemoryTable.get( typeName ).get( STATIC_FIELD_ID );
	}

	private synchronized void loadInMemoryTable()
	{
		try
		{
			inMemoryTable.clear();
			for( Map<String, Object> dbRow : select( null ) )
			{
				inMemoryTable.put( ( String )dbRow.get( STATIC_FIELD_TYPE ), dbRow );
			}
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to initialize in-memory table for: " + this, e );
		}
	}
}
