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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.TableDescriptor;

public class SourceMetadataTable extends TableBase
{
	public static final String STATIC_FIELD_SOURCE_NAME = "__SOURCE";

	public static final String SOURCE_TABLE_NAME = "source_metadata";

	private Set<String> inMemoryTable = new HashSet<String>();

	public SourceMetadataTable( String dbName, ConnectionRegistry connectionRegistry, DatabaseMetadata databaseMetadata,
			TableRegistry tableRegistry ) throws SQLException
	{
		super( dbName, SOURCE_TABLE_NAME, null, connectionRegistry, databaseMetadata, tableRegistry );

		try
		{
			for( Map<String, Object> dbRow : select( null ) )
			{
				inMemoryTable.add( ( String )dbRow.get( STATIC_FIELD_SOURCE_NAME ) );
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
		inMemoryTable.add( ( String )data.get( STATIC_FIELD_SOURCE_NAME ) );
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

		descriptor.addToPkSequence( STATIC_FIELD_SOURCE_NAME );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return true;
	}

	public synchronized String[] getSourceNames()
	{
		return inMemoryTable.toArray( new String[0] );
	}

	public synchronized boolean doesInMemoryTableContain( String sourceName )
	{
		return inMemoryTable.contains( sourceName );
	}

}
