/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.statistics.store;

import java.util.HashMap;

import com.eviware.loadui.impl.statistics.store.table.MetadataProvider;

/**
 * Keeps database specific information.
 * 
 * @author predrag.vucetic
 * 
 */
public class DatabaseMetadata implements MetadataProvider
{
	/**
	 * "CREATE TABLE" DDL expression
	 */
	private String createTableExpression;

	/**
	 * DDL expression for creating primary key index
	 */
	private String addPrimaryKeyIndexExpression;

	/**
	 * Map which contains java to database types mapping
	 */
	private HashMap<Class<? extends Object>, String> typeConversionMap = new HashMap<Class<? extends Object>, String>();

	/**
	 * Adds java to database type conversion pair
	 * 
	 * @param clazz
	 *           Java type
	 * @param databaseType
	 *           Database type
	 */
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
