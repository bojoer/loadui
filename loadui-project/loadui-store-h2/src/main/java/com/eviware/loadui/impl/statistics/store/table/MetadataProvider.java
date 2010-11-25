package com.eviware.loadui.impl.statistics.store.table;

import java.util.HashMap;

public interface MetadataProvider
{
	public HashMap<Class<? extends Object>, String> getTypeConversionMap();
	
	public String getCreateTableExpression();
	
	public String getAddPrimaryKeyIndexExpression();
}
