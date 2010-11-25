package com.eviware.loadui.impl.statistics.store;

import java.util.HashMap;

import com.eviware.loadui.impl.statistics.store.table.MetadataProvider;

public class DatabaseMetadata implements MetadataProvider
{
	private String createTableExpression;

	private String addPrimaryKeyIndexExpression;

	private HashMap<Class<? extends Object>, String> typeConversionMap = new HashMap<Class<? extends Object>, String>();

	public void addTypeConversionPair( Class<? extends Object> clazz, String databaseType )
	{
		typeConversionMap.put( clazz, databaseType );
	}

	@Override
	public HashMap<Class<? extends Object>, String> getTypeConversionMap()
	{
		return typeConversionMap;
	}
	
	public void setCreateTableExpression( String createTableExpression )
	{
		this.createTableExpression = createTableExpression;
	}

	@Override
	public String getCreateTableExpression()
	{
		return createTableExpression;
	}

	public void setAddPrimaryKeyIndexExpression( String addPrimaryKeyIndexExpression )
	{
		this.addPrimaryKeyIndexExpression = addPrimaryKeyIndexExpression;
	}

	@Override
	public String getAddPrimaryKeyIndexExpression()
	{
		return addPrimaryKeyIndexExpression;
	}

}
