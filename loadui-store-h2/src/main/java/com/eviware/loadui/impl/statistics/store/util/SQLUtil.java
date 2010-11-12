package com.eviware.loadui.impl.statistics.store.util;

import java.util.Iterator;
import java.util.Map;

public class SQLUtil
{

	public static InsertStatementHolder createDataTableInsertScript( String tableName, String tstampColumn,
			Map<String, Class<? extends Object>> tableStructureMap )
	{
		InsertStatementHolder sh = new InsertStatementHolder();
		Iterator<String> keys = tableStructureMap.keySet().iterator();
		StringBuffer names = new StringBuffer();
		names.append( tstampColumn + ", " );
		StringBuffer values = new StringBuffer();
		values.append( "?, " );
		String key;
		while( keys.hasNext() )
		{
			key = keys.next();
			names.append( key );
			values.append( "?" );
			if( keys.hasNext() )
			{
				names.append( ", " );
				values.append( ", " );
			}
			sh.addArgument( key );
		}
		sh.setStatementSql( "INSERT INTO " + tableName + "( " + names.toString() + " ) VALUES ( " + values.toString()
				+ " )" );
		return sh;
	}

	public static String createTimestampTableCreateScript( String createTableExpr, String tableName, String timeColName,
			Class<? extends Number> timeColType, String pKeyExpr, Map<String, Class<? extends Object>> tableStructureMap,
			Map<Class<? extends Object>, String> typeConversionMap )
	{
		StringBuffer b = new StringBuffer();
		b.append( createTableExpr );
		b.append( " " );
		b.append( tableName );
		b.append( " ( " );
		b.append( timeColName );
		b.append( " " );
		b.append( typeConversionMap.get( timeColType ) );
		b.append( " " );
		b.append( pKeyExpr );
		b.append( ", " );

		Iterator<String> keys = tableStructureMap.keySet().iterator();
		String key;
		while( keys.hasNext() )
		{
			key = keys.next();
			b.append( key );
			b.append( " " );
			b.append( typeConversionMap.get( tableStructureMap.get( key ) ) );
			if( keys.hasNext() )
			{
				b.append( ", " );
			}
		}
		b.append( " ) " );
		return b.toString();
	}

}
