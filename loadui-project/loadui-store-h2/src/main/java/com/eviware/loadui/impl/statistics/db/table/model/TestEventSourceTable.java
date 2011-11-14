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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.TableDescriptor;

public class TestEventSourceTable extends TableBase
{
	public static final String STATIC_FIELD_ID = "_ID";
	public static final String STATIC_FIELD_TYPEID = "_TYPEID";
	public static final String STATIC_FIELD_LABEL = "_LABEL";
	public static final String STATIC_FIELD_HASH = "_HASH";
	public static final String STATIC_FIELD_DATA = "_DATA";

	public static final String TABLE_NAME = "test_event_source";

	private Map<String, Map<String, Object>> inMemoryTable = new HashMap<String, Map<String, Object>>();

	public TestEventSourceTable( String dbName, ConnectionRegistry connectionRegistry,
			DatabaseMetadata databaseMetadata, TableRegistry tableRegistry ) throws SQLException
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
		descriptor.addStaticField( STATIC_FIELD_TYPEID, Long.class );
		descriptor.addStaticField( STATIC_FIELD_LABEL, String.class );
		descriptor.addStaticField( STATIC_FIELD_HASH, String.class );
		descriptor.addStaticField( STATIC_FIELD_DATA, Byte[].class );

		descriptor.setAutoIncrementPK( STATIC_FIELD_ID );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return true;
	}

	public Map<String, Map<String, Object>> getInMemoryTable()
	{
		return inMemoryTable;
	}

	/**
	 * Returns all source of appropriate ID.
	 */
	public synchronized Map<String, Object> getById( Long id )
	{
		for( Entry<String, Map<String, Object>> entry : inMemoryTable.entrySet() )
		{
			if( id.equals( ( Long )entry.getValue().get( STATIC_FIELD_ID ) ) )
			{
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Returns all sources of appropriate type, specified by type ID.
	 * 
	 * @param typeId
	 *           Find all sources with this type ID.
	 * @return List of sources of specified type.
	 */
	public synchronized List<Map<String, Object>> getByTypeId( Long typeId )
	{
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for( Entry<String, Map<String, Object>> entry : inMemoryTable.entrySet() )
		{
			if( typeId.equals( ( Long )entry.getValue().get( STATIC_FIELD_TYPEID ) ) )
			{
				result.add( entry.getValue() );
			}
		}
		return result;
	}

	/**
	 * Loads all data in memory.
	 */
	private synchronized void loadInMemoryTable()
	{
		try
		{
			inMemoryTable.clear();
			List<Map<String, Object>> result = select( null );
			for( Map<String, Object> row : result )
			{
				inMemoryTable.put( ( String )row.get( STATIC_FIELD_HASH ), row );
			}

		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to initialize in-memory table for: " + this, e );
		}
	}

	/**
	 * Retrieves IDs of sources specified by their hash values.
	 * 
	 * @param hashes
	 *           List of source hashes
	 * @return List of source IDs which hash values are in input
	 *         <code>hashes</code> list.
	 */
	public synchronized List<Long> getIdsByHash( List<String> hashes )
	{
		List<Long> result = new ArrayList<Long>();
		for( Entry<String, Map<String, Object>> item : inMemoryTable.entrySet() )
		{
			if( hashes.size() == 0 || hashes.contains( ( String )item.getValue().get( STATIC_FIELD_HASH ) ) )
			{
				result.add( ( Long )item.getValue().get( STATIC_FIELD_ID ) );
			}
		}
		return result;
	}

	/**
	 * Retrieves source ID by specified hash value.
	 * 
	 * @param hash
	 *           Source hash value
	 * @return ID of source with hash equal to input argument.
	 */
	public synchronized Long getIdByHash( String hash )
	{
		return ( Long )inMemoryTable.get( hash ).get( TestEventSourceTable.STATIC_FIELD_ID );
	}
}
