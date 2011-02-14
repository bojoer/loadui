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

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.TableDescriptor;

public class ExecutionMetadataTable extends TableBase
{
	public static final String TABLE_NAME = "EXECUTION_METADATA";

	public static final String STATIC_FIELD_NAME = "__NAME";
	public static final String STATIC_FIELD_START_TIME = "__START_TIME";
	public static final String STATIC_FIELD_ARCHIVED = "__ARCHIVED";
	public static final String STATIC_FIELD_LABEL = "__LABEL";

	public static final String STATEMENT_ARCHIVE = "archiveStatement";
	public static final String STATEMENT_UPDATE_LABEL = "updateLabelStatement";

	public ExecutionMetadataTable( String dbName, ConnectionRegistry connectionRegistry,
			DatabaseMetadata databaseMetadata, TableRegistry tableRegistry )
	{
		super( dbName, TABLE_NAME, null, connectionRegistry, databaseMetadata, tableRegistry );

		prepareStatement( STATEMENT_ARCHIVE, "update " + getTableName() + " set " + STATIC_FIELD_ARCHIVED + " = TRUE " );
		prepareStatement( STATEMENT_UPDATE_LABEL, "update " + getTableName() + " set " + STATIC_FIELD_LABEL + " = ? " );
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_NAME, String.class );
		descriptor.addStaticField( STATIC_FIELD_START_TIME, Long.class );
		descriptor.addStaticField( STATIC_FIELD_ARCHIVED, Boolean.class );
		descriptor.addStaticField( STATIC_FIELD_LABEL, String.class );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return false;
	}

	public void archive() throws SQLException
	{
		execute( STATEMENT_ARCHIVE, null );
	}

	public void setLabel( String label ) throws SQLException
	{
		execute( STATEMENT_UPDATE_LABEL, new Object[] { label } );
	}

}
