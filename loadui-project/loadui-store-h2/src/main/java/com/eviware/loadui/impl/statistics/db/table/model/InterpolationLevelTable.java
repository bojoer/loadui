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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.TableDescriptor;

public class InterpolationLevelTable extends TableBase
{
	public static final String STATIC_FIELD_INTERPOLATION_LEVEL = "__LEVEL";

	public static final String INTERPOLATION_LEVEL_TABLE_NAME = "level_metadata";

	private Set<Integer> inMemoryTable = Collections.synchronizedSet( new HashSet<Integer>() );

	public InterpolationLevelTable( String dbName, ConnectionRegistry connectionRegistry,
			DatabaseMetadata databaseMetadata, TableRegistry tableRegistry ) throws SQLException
	{
		super( dbName, INTERPOLATION_LEVEL_TABLE_NAME, null, connectionRegistry, databaseMetadata, tableRegistry );

		try
		{
			List<Map<String, Object>> result = select( null );
			for( Map<String, Object> dbRow : result )
			{
				inMemoryTable.add( ( Integer )dbRow.get( STATIC_FIELD_INTERPOLATION_LEVEL ) );
			}
		}
		catch( SQLException e )
		{
			throw new RuntimeException( "Unable to initialize in-memory table for: " + this, e );
		}
	}

	@Override
	public void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		super.insert( data );
		inMemoryTable.add( ( Integer )data.get( STATIC_FIELD_INTERPOLATION_LEVEL ) );
	}

	@Override
	public void release()
	{
		super.release();
		inMemoryTable.clear();
		inMemoryTable = null;
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_INTERPOLATION_LEVEL, Integer.class );
		descriptor.addToPkSequence( STATIC_FIELD_INTERPOLATION_LEVEL );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return true;
	}

	public Integer[] getLevels()
	{
		return inMemoryTable.toArray( new Integer[0] );
	}

	public Set<Integer> getInMemoryTable()
	{
		return inMemoryTable;
	}

}
