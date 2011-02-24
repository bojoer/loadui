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
package com.eviware.loadui.impl.statistics.db.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.eviware.loadui.api.model.Releasable;

public class PropertiesRegistry implements Releasable
{
	private Map<String, PropertiesBase> propertiesMap = new HashMap<String, PropertiesBase>();

	/**
	 * Adds property into a registry.
	 * 
	 * @param executionId
	 * @param properties
	 */
	public void put( String executionId, PropertiesBase properties )
	{
		propertiesMap.put( executionId + properties.getName(), properties );
	}

	/**
	 * Retrieves properties specified by execution and properties name.
	 * 
	 * @param executionId
	 * @param propertiesName
	 * @return
	 */
	public PropertiesBase getProperties( String executionId, String propertiesName )
	{
		PropertiesBase pb = propertiesMap.get( executionId + propertiesName );
		if( pb == null )
		{
			throw new IllegalArgumentException( "Properties " + propertiesName + " does not exist in execution: "
					+ executionId );
		}
		return pb;
	}

	/**
	 * Retrieves all properties for specified execution.
	 * 
	 * @param executionId
	 * @return
	 */
	public List<PropertiesBase> getAllProperties( String executionId )
	{
		List<PropertiesBase> result = new ArrayList<PropertiesBase>();
		Iterator<String> keys = propertiesMap.keySet().iterator();
		while( keys.hasNext() )
		{
			String key = keys.next();
			if( key.startsWith( executionId ) )
			{
				result.add( propertiesMap.get( key ) );
			}
		}
		return result;
	}

	/**
	 * Releases all properties for a specified execution
	 * 
	 * @param executionId
	 *           Execution ID
	 */
	public void release( String executionId )
	{
		List<PropertiesBase> propertiesList = getAllProperties( executionId );
		for( int i = 0; i < propertiesList.size(); i++ )
		{
			release( executionId, propertiesList.get( i ).getName() );
		}
	}

	/**
	 * Releases specified properties
	 * 
	 * @param executionId
	 *           Execution ID
	 * @param propertiesName
	 *           Properties name
	 */
	public void release( String executionId, String propertiesName )
	{
		PropertiesBase dtd = propertiesMap.get( executionId + propertiesName );
		if( dtd != null )
		{
			propertiesMap.remove( executionId + propertiesName );
		}
	}

	/**
	 * Releases all properties
	 */
	@Override
	public void release()
	{
		propertiesMap.clear();
	}
}
