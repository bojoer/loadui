package com.eviware.loadui.impl.statistics.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.impl.statistics.store.table.TableBase;
import com.eviware.loadui.impl.statistics.store.table.TableProvider;

public class TableRegistry implements TableProvider
{
	private Map<String, TableBase> tableMap = new HashMap<String, TableBase>();

	public void put( String executionId, TableBase table )
	{
		tableMap.put( executionId + table.getExternalName(), table );
	}

	@Override
	public TableBase getTable( String dbName, String tableName )
	{
		TableBase dtd = tableMap.get( dbName + tableName );
		if( dtd == null )
		{
			throw new IllegalArgumentException( "Table " + tableName + " does not exist in execution: " + dbName );
		}
		return dtd;
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

	public void dispose( String dbName, String tableName )
	{
		TableBase dtd = tableMap.get( dbName + tableName );
		if( dtd != null )
		{
			dtd.dispose();
			tableMap.remove( dbName + tableName );
		}
	}

	public void dispose()
	{
		Iterator<String> keys = tableMap.keySet().iterator();
		while( keys.hasNext() )
		{
			tableMap.get( keys.next() ).dispose();
		}
		tableMap.clear();
	}
}
