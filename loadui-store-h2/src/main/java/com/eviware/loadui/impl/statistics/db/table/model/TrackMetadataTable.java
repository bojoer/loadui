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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.impl.statistics.db.ConnectionRegistry;
import com.eviware.loadui.impl.statistics.db.DatabaseMetadata;
import com.eviware.loadui.impl.statistics.db.TableRegistry;
import com.eviware.loadui.impl.statistics.db.table.TableBase;
import com.eviware.loadui.impl.statistics.db.table.TableDescriptor;

public class TrackMetadataTable extends TableBase
{
	public static final String SELECT_ARG_TRACKNAME_EQ = "track_eq";

	public static final String TABLE_NAME = "track_metadata";

	public static final String STATIC_FIELD_TRACK_NAME = "__TRACK_ID";

	public static final String STATEMENT_LIST_TRACK_NAMES = "listTrackNamesStatement";
	
	public TrackMetadataTable( String dbName, ConnectionRegistry connectionRegistry, DatabaseMetadata databaseMetadata,
			TableRegistry tableRegistry )
	{
		super( dbName, TABLE_NAME, null, connectionRegistry, databaseMetadata, tableRegistry );

		prepareStatement( STATEMENT_LIST_TRACK_NAMES, "select " + STATIC_FIELD_TRACK_NAME + " from " + getTableName() );
	}

	@Override
	public synchronized void insert( Map<String, ? extends Object> data ) throws SQLException
	{
		Map<String, Object> queryData = new HashMap<String, Object>();
		queryData.put( SELECT_ARG_TRACKNAME_EQ, data.get( STATIC_FIELD_TRACK_NAME ) );
		if( select( queryData ).size() == 0 )
		{
			super.insert( data );
		}
	}

	@Override
	protected void initializeDescriptor( TableDescriptor descriptor )
	{
		descriptor.addStaticField( STATIC_FIELD_TRACK_NAME, String.class );
		descriptor.addToPkSequence( STATIC_FIELD_TRACK_NAME );
		descriptor.addSelectCriteria( SELECT_ARG_TRACKNAME_EQ, STATIC_FIELD_TRACK_NAME, "=?" );
	}

	@Override
	protected boolean useTableSpecificConnection()
	{
		return true;
	}

	public synchronized List<String> listAllTracks() throws SQLException
	{
		List<String> resultList = new ArrayList<String>();
		ResultSet result = executeQuery(STATEMENT_LIST_TRACK_NAMES, null );
		while( result.next() )
		{
			resultList.add( result.getString( STATIC_FIELD_TRACK_NAME ) );
		}
		return resultList;
	}

}
