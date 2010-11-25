package com.eviware.loadui.impl.statistics.store.table;


public interface TableProvider
{
	public TableBase getTable( String dbName, String tableName );
}
