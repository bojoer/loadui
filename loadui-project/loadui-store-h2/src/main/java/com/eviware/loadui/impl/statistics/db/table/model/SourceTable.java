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
package com.eviware.loadui.impl.statistics.db.table.model;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.TableDescriptor;

public class SourceTable extends TableBase
{
	public static final String STATIC_FIELD_SOURCE_NAME = "__SOURCE";
	public static final String STATIC_FIELD_SOURCEID = "__SOURCE_ID";

	private Map<String, Integer> inMemoryTable = new HashMap<String, Integer>();

	public SourceTable( String dbName, String name, ConnectionRegistry connectionRegistry,
			DatabaseMetadata databaseMetadata, TableRegistry tableRegistry ) throws SQLException
	{
		super( dbName, name, null, connectionRegistry, databaseMetadata, tableRegistry );

		try
		{
			List<Map<String, Object>> result = select( null );
			for( int i = 0; i < result.size(); i++ )
			{
				inMemoryTable.put( ( String )result.get( i ).get( STATIC_FIELD_SOURCE_NAME ), ( Integer )result.get( i )
						.get( STATIC_FIELD_SOURCEID ) );
			}
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to initialize in-memory table for: " + this, e );
		}
	}

	@Override
	public synchronized void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		super.insert( data );
		inMemoryTable.put( ( String )data.get( STATIC_FIELD_SOURCE_NAME ), ( Integer )data.get( STATIC_FIELD_SOURCEID ) );
	}

	public synchronized Integer getSourceId( String source ) throws SQLException
	{
		if( inMemoryTable.get( source ) == null )
		{
			// source id for this source is not generated yet, so use sequence
			// table to generate it
			SequenceTable st = ( SequenceTable )getTable( SequenceTable.SEQUENCE_TABLE_NAME );
			Integer sourceId = st.next( getExternalName(), STATIC_FIELD_SOURCEID );

			// insert newly created source id into this table (insert into
			// inMemoryTable is done in 'insert' method implementation after all
			// SQL operations are finished successfully). If following insert
			// fails,
			// previously generated id will be left unused.
			Map<String, Object> data = new HashMap<String, Object>();
			data.put( STATIC_FIELD_SOURCE_NAME, source );
			data.put( STATIC_FIELD_SOURCEID, sourceId );
			insert( data );
			commit();
		}
		return inMemoryTable.get( source );
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
		descriptor.addStaticField( STATIC_FIELD_SOURCE_NAME, String.class );
		descriptor.addStaticField( STATIC_FIELD_SOURCEID, Integer.class );

		descriptor.addToPkSequence( STATIC_FIELD_SOURCE_NAME );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return false;
	}

}
