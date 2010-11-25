package com.eviware.loadui.impl.statistics.store.table;

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
