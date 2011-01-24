/*
 * Copyright 2010 eviware software ab
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

import java.sql.SQLException;
import java.util.Map;

import com.eviware.loadui.impl.statistics.store.table.ConnectionProvider;
import com.eviware.loadui.impl.statistics.store.table.MetadataProvider;
import com.eviware.loadui.impl.statistics.store.table.TableBase;
import com.eviware.loadui.impl.statistics.store.table.TableDescriptor;
import com.eviware.loadui.impl.statistics.store.table.TableProvider;

public class DataTable extends TableBase
{
	public static final String SELECT_ARG_TIMESTAMP_GTE = "tstamp_gte";
	public static final String SELECT_ARG_TIMESTAMP_LTE = "tstamp_lte";
	public static final String SELECT_ARG_SOURCEID_EQ = "sourceid_eq";
	public static final String SELECT_ARG_INTERPOLATIONLEVEL_EQ = "interplvl_eq";
	
	public static final String STATIC_FIELD_TIMESTAMP = "_TSTAMP";
	public static final String STATIC_FIELD_SOURCEID = "_SOURCE_ID";
	public static final String STATIC_FIELD_INTERPOLATIONLEVEL = "_INTERP_LVL";

	public DataTable( String dbName, String name, Map<String, ? extends Class<? extends Object>> dynamicFields,
			ConnectionProvider connectionProvider, MetadataProvider metadataProvider, TableProvider tableProvider )
	{
		super( dbName, name, dynamicFields, connectionProvider, metadataProvider, tableProvider );
	}

	@Override
	public void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		super.insert( data );
		//TODO commit here for now, maybe this will have to change
		commit();
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_TIMESTAMP, Integer.class );
		descriptor.addStaticField( STATIC_FIELD_SOURCEID, Integer.class );
		descriptor.addStaticField( STATIC_FIELD_INTERPOLATIONLEVEL, Integer.class );

		descriptor.addToPkSequence( STATIC_FIELD_TIMESTAMP );
		descriptor.addToPkSequence( STATIC_FIELD_SOURCEID );
		descriptor.addToPkSequence( STATIC_FIELD_INTERPOLATIONLEVEL );

		descriptor.addSelectCriteria( SELECT_ARG_TIMESTAMP_GTE, STATIC_FIELD_TIMESTAMP, ">=?" );
		descriptor.addSelectCriteria( SELECT_ARG_TIMESTAMP_LTE, STATIC_FIELD_TIMESTAMP, "<=?" );
		descriptor.addSelectCriteria( SELECT_ARG_SOURCEID_EQ, STATIC_FIELD_SOURCEID, "=?" );
		descriptor.addSelectCriteria( SELECT_ARG_INTERPOLATIONLEVEL_EQ, STATIC_FIELD_INTERPOLATIONLEVEL, "=?" );
	}

}
