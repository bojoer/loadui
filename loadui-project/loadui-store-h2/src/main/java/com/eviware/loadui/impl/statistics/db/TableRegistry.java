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
package com.eviware.loadui.impl.statistics.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.statistics.db.table.TableBase;

public class TableRegistry implements Releasable
{
	private final Map<String, TableBase> tableMap = new HashMap<String, TableBase>();

	public void putAll( String dbName, List<TableBase> tableList )
	{
		for( TableBase table : tableList )
		{
			tableMap.put( dbName + table.getExternalName(), table );
		}
	}

	public void put( String dbName, TableBase table )
	{
		tableMap.put( dbName + table.getExternalName(), table );
	}

	public TableBase getTable( String dbName, String tableName )
	{
		return tableMap.get( dbName + tableName );
	}

	public List<TableBase> getAllTables( String dbName )
	{
		List<TableBase> result = new ArrayList<TableBase>();
		Iterator<String> keys = tableMap.keySet().iterator();
		while( keys.hasNext() )
		{
			String key = keys.next();
			if( key.startsWith( dbName ) )
			{
				result.add( tableMap.get( key ) );
			}
		}
		return result;
	}

	/**
	 * Releases all table for a specified database
	 * 
	 * @param dbName
	 *           Database name
	 */
	public void release( String dbName )
	{
		for( TableBase tb : getAllTables( dbName ) )
		{
			release( dbName, tb.getExternalName() );
		}
	}

	/**
	 * Releases specified table
	 * 
	 * @param dbName
	 *           Table database
	 * @param tableName
	 *           Table name
	 */
	public void release( String dbName, String tableName )
	{
		TableBase dtd = tableMap.get( dbName + tableName );
		if( dtd != null )
		{
			dtd.release();
			tableMap.remove( dbName + tableName );
		}
	}

	/**
	 * Releases all tables
	 */
	@Override
	public void release()
	{
		Iterator<String> keys = tableMap.keySet().iterator();
		while( keys.hasNext() )
		{
			tableMap.get( keys.next() ).release();
		}
		tableMap.clear();
	}
}
