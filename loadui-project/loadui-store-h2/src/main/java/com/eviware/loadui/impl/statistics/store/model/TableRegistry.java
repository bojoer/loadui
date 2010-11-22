package com.eviware.loadui.impl.statistics.store.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TableRegistry
{
	private Map<String, TableBase> tableMap = new HashMap<String, TableBase>();

	private Map<String, MetaTable> metaTableMap = new HashMap<String, MetaTable>();
	
	public void put( String executionId, TableBase table )
	{
		tableMap.put( executionId + "_" + table.getExternalName(), table );
	}

	public TableBase get( String executionId, String tableName )
	{
		TableBase dtd = tableMap.get( executionId + "_" + tableName );
		if( dtd == null )
		{
			throw new IllegalArgumentException( "Table definition for execution: " + executionId + " and track: "
					+ tableName + " does not exist!" );
		}
		return dtd;
	}

	public List<TableBase> get( String executionId )
	{
		List<TableBase> result = new ArrayList<TableBase>();
		Iterator<String> keys = tableMap.keySet().iterator();
		while( keys.hasNext() )
		{
			String key = keys.next();
			if( key.startsWith( executionId ) )
			{
				result.add( tableMap.get( key ) );
			}
		}
		return result;
	}

	public void putMetaTable( String executionId, MetaTable table )
	{
		metaTableMap.put( executionId, table );
	}

	public MetaTable getMetaTable( String executionId )
	{
		MetaTable dtd = metaTableMap.get( executionId );
		if( dtd == null )
		{
			throw new IllegalArgumentException( "Meta table for execution: " + executionId + " does not exist!" );
		}
		return dtd;
	}
	
	public void dispose()
	{
		Iterator<String> keys = tableMap.keySet().iterator();
		while( keys.hasNext() )
		{
			tableMap.get( keys.next() ).dispose();
		}
		keys = metaTableMap.keySet().iterator();
		while( keys.hasNext() )
		{
			metaTableMap.get( keys.next() ).dispose();
		}
	}
}
