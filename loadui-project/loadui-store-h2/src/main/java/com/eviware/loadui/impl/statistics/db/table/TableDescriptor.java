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
package com.eviware.loadui.impl.statistics.db.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableDescriptor
{
	private List<String[]> selectCriteria = new ArrayList<String[]>();

	private Map<String, Class<? extends Object>> staticFields = new HashMap<String, Class<? extends Object>>();

	private List<String> pkSequence = new ArrayList<String>();

	public void addStaticField( String name, Class<? extends Object> type )
	{
		staticFields.put( name, type );
	}

	public void addToPkSequence( String field )
	{
		pkSequence.add( field );
	}

	public void addSelectCriteria( String argumentName, String field, String criteria )
	{
		selectCriteria.add( new String[] { argumentName, field, criteria } );
	}
	
	public List<String> getPkSequence()
	{
		return pkSequence;
	}
	
	public List<String[]> getSelectCriteria()
	{
		return selectCriteria;
	}
	
	public Map<String, Class<? extends Object>> getStaticFields()
	{
		return staticFields;
	}
}
