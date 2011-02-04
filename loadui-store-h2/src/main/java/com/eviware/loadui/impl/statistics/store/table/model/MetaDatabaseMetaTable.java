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
package com.eviware.loadui.impl.statistics.store.table.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.impl.statistics.store.table.ConnectionProvider;
import com.eviware.loadui.impl.statistics.store.table.MetadataProvider;
import com.eviware.loadui.impl.statistics.store.table.TableBase;
import com.eviware.loadui.impl.statistics.store.table.TableDescriptor;
import com.eviware.loadui.impl.statistics.store.table.TableProvider;
import com.eviware.loadui.impl.statistics.store.util.JdbcUtil;

public class MetaDatabaseMetaTable extends TableBase
{
	public static final String SELECT_ARG_EXECUTION_NAME_EQ = "execution_name_eq";
	
	private static final String METATABLE_NAME = "meta_table";
	
	public static final String STATIC_FIELD_TSTAMP = "_TSTAMP";
	public static final String STATIC_FIELD_EXECUTION_NAME = "_EXECUTION";

	public static final String STATEMENT_LIST_EXECUTIONS = "LIST_EXECUTIONS";

	public MetaDatabaseMetaTable( String dbName, ConnectionProvider connectionProvider, MetadataProvider metadataProvider, TableProvider tableProvider )
	{
		super( dbName, METATABLE_NAME, null, connectionProvider, metadataProvider, tableProvider );

		prepareStatement( STATEMENT_LIST_EXECUTIONS, "select " + STATIC_FIELD_EXECUTION_NAME + " from " + getTableName() );
	}

	@Override
	public void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		super.insert( data );
		// TODO commit for now.
		commit();
	}

	public boolean exist( String executionId ) throws SQLException
	{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put( SELECT_ARG_EXECUTION_NAME_EQ, executionId );
		List<Map<String, Object>> result = select( data );
		return result.size() > 0;
	}

	public List<String> list() throws SQLException
	{
		List<String> result = new ArrayList<String>();
		ResultSet rs = executeQuery( STATEMENT_LIST_EXECUTIONS, null );
		if( rs != null )
		{
			while( rs.next() )
			{
				result.add( rs.getString( STATIC_FIELD_EXECUTION_NAME ) );
			}
		}
		JdbcUtil.close( rs );
		return result;
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_TSTAMP, Long.class );
		descriptor.addStaticField( STATIC_FIELD_EXECUTION_NAME, String.class );
		descriptor.addToPkSequence( STATIC_FIELD_EXECUTION_NAME );
		descriptor.addSelectCriteria( SELECT_ARG_EXECUTION_NAME_EQ, STATIC_FIELD_EXECUTION_NAME, "=?" );
	}
}
