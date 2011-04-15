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

public class SequenceTable extends TableBase
{
	public static final String SELECT_ARG_TABLE_EQ = "table_eq";
	public static final String SELECT_ARG_COLUMN_EQ = "column_eq";

	public static final String SEQUENCE_TABLE_NAME = "sequence_table";

	public static final String STATIC_FIELD_TABLE = "__TABLE";
	public static final String STATIC_FIELD_COLUMN = "__COLUMN";
	public static final String STATIC_FIELD_VALUE = "__VALUE";

	public static final String STATEMENT_UPDATE_VALUE = "updateValueStatement";

	public SequenceTable( String dbName, ConnectionRegistry connectionRegistry, DatabaseMetadata databaseMetadata,
			TableRegistry tableRegistry ) throws SQLException
	{
		super( dbName, SEQUENCE_TABLE_NAME, null, connectionRegistry, databaseMetadata, tableRegistry );

		prepareStatement( STATEMENT_UPDATE_VALUE, "update " + getTableName() + " set " + STATIC_FIELD_VALUE
				+ " = ? where " + STATIC_FIELD_TABLE + " = ? and " + STATIC_FIELD_COLUMN + " = ?" );
	}

	public synchronized Integer next( String tableName, String column ) throws SQLException
	{
		Integer id;
		Map<String, Object> data = new HashMap<String, Object>();
		data.put( SELECT_ARG_TABLE_EQ, tableName );
		data.put( SELECT_ARG_COLUMN_EQ, column );
		List<Map<String, Object>> rs = select( data );
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
		return id;
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_TABLE, String.class );
		descriptor.addStaticField( STATIC_FIELD_COLUMN, String.class );
		descriptor.addStaticField( STATIC_FIELD_VALUE, Integer.class );

		descriptor.addToPkSequence( STATIC_FIELD_TABLE );
		descriptor.addToPkSequence( STATIC_FIELD_COLUMN );

		descriptor.addSelectCriteria( SELECT_ARG_TABLE_EQ, STATIC_FIELD_TABLE, "=?" );
		descriptor.addSelectCriteria( SELECT_ARG_COLUMN_EQ, STATIC_FIELD_COLUMN, "=?" );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return true;
	}

}
