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
import java.util.Map;

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.TableDescriptor;

public class DataTable extends TableBase
{
	public static final String SELECT_ARG_TIMESTAMP_GTE = "tstamp_gte";
	public static final String SELECT_ARG_TIMESTAMP_LTE = "tstamp_lte";
	public static final String SELECT_ARG_SOURCEID_EQ = "sourceid_eq";
	
	public static final String STATIC_FIELD_TIMESTAMP = "_TSTAMP";
	public static final String STATIC_FIELD_SOURCEID = "_SOURCE_ID";

	public DataTable( String dbName, String name, Map<String, ? extends Class<? extends Object>> dynamicFields,
			ConnectionRegistry connectionRegistry, DatabaseMetadata databaseMetadata, TableRegistry tableRegistry ) throws SQLException
	{
		super( dbName, name, dynamicFields, connectionRegistry, databaseMetadata, tableRegistry );
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_TIMESTAMP, Long.class );
		descriptor.addStaticField( STATIC_FIELD_SOURCEID, Integer.class );

		descriptor.addToPkSequence( STATIC_FIELD_TIMESTAMP );
		descriptor.addToPkSequence( STATIC_FIELD_SOURCEID );

		descriptor.addSelectCriteria( SELECT_ARG_TIMESTAMP_GTE, STATIC_FIELD_TIMESTAMP, ">=?" );
		descriptor.addSelectCriteria( SELECT_ARG_TIMESTAMP_LTE, STATIC_FIELD_TIMESTAMP, "<=?" );
		descriptor.addSelectCriteria( SELECT_ARG_SOURCEID_EQ, STATIC_FIELD_SOURCEID, "=?" );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return false;
	}
}
