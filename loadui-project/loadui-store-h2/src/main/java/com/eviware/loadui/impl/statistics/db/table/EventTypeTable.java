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
package com.eviware.loadui.impl.statistics.db.table;

import java.sql.SQLException;
import java.util.Map;

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;

public class EventTypeTable extends TableBase
{
	private static final String STATIC_FIELD_EVENTTYPE_ID = "__ID";
	private static final String STATIC_FIELD_EVENTTYPE_LABEL = "EVENTTYPE";
	private static final String STATIC_FIELD_EVENTTYPE_TYPE = "TYPE";

	public EventTypeTable( String dbName, String name, Map<String, ? extends Class<? extends Object>> dynamicFields,
			ConnectionRegistry connectionRegistry, DatabaseMetadata databaseMetadata, TableRegistry tableRegistry )
			throws SQLException
	{
		super( dbName, name, dynamicFields, connectionRegistry, databaseMetadata, tableRegistry );
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_EVENTTYPE_ID, Integer.class );
		descriptor.addStaticField( STATIC_FIELD_EVENTTYPE_LABEL, String.class );
		descriptor.addStaticField( STATIC_FIELD_EVENTTYPE_TYPE, String.class );

		descriptor.addToPkSequence( STATIC_FIELD_EVENTTYPE_ID );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return true;
	}

}
