/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.impl.summary;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.table.TableModel;

import com.eviware.loadui.api.summary.MutableSection;

public class MutableSectionImpl implements MutableSection
{

	private LinkedHashMap<String, TableModel> tables = new LinkedHashMap<String, TableModel>();
	private LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
	private String title;

	public MutableSectionImpl( String title )
	{
		this.title = title;
	}

	@Override
	public void addTable( String name, TableModel model )
	{
		tables.put( name, model );
	}

	@Override
	public void addValue( String name, String value )
	{
		values.put( name, value );
	}

	public void clearValues()
	{
		values.clear();
	}

	public void clearTables()
	{
		tables.clear();
	}

	public String getValue( String name )
	{
		return values.get( name );
	}

	public TableModel getTableModel( String name )
	{
		return tables.get( name );
	}

	public Set<String> getValueNames()
	{
		return values.keySet();
	}

	public Set<String> getTableNames()
	{
		return tables.keySet();
	}

	public String getTitle()
	{
		return title;
	}

	@Override
	public Map<String, TableModel> getTables()
	{
		return tables;
	}

	@Override
	public Map<String, String> getValues()
	{
		return values;
	}
}
