package com.eviware.loadui.impl.statistics.store.table;

import java.util.HashMap;

/**
 * Represents database specific data, like type conversion, DDL statements etc.
 * Implementation of this class should provide data specific to the database
 * which is in use.
 * 
 * @author predrag.vucetic
 */
public interface MetadataProvider
{
	/**
	 * Gets the map which contains mapping between java and database types.
	 * 
	 * @return
	 */
	public HashMap<Class<? extends Object>, String> getTypeConversionMap();

	/**
	 * Gets CREATE TABLE expression.
	 * 
	 * @return
	 */
	public String getCreateTableExpression();

	/**
	 * Gets the expression for creating primary key index.
	 * 
	 * @return
	 */
	public String getAddPrimaryKeyIndexExpression();
}
