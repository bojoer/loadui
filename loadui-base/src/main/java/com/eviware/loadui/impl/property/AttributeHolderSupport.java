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
package com.eviware.loadui.impl.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.config.PropertyConfig;
import com.eviware.loadui.config.PropertyListConfig;
import com.eviware.loadui.util.StringUtils;

public class AttributeHolderSupport implements AttributeHolder
{
	private final Map<String, String> attributes = new HashMap<String, String>();
	private final PropertyListConfig config;

	public AttributeHolderSupport( PropertyListConfig config )
	{
		this.config = config;

		for( PropertyConfig attr : config.getPropertyArray() )
			attributes.put( attr.getKey(), attr.getStringValue() );
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		return attributes.containsKey( key ) ? attributes.get( key ) : defaultValue;
	}

	@Override
	public void setAttribute( String key, String value )
	{
		if( attributes.containsKey( key ) )
		{
			for( int i = config.sizeOfPropertyArray() - 1; i >= 0; i-- )
			{
				if( key.equals( config.getPropertyArray( i ).getKey() ) )
				{
					if( StringUtils.isNullOrEmpty( value ) )
					{
						config.removeProperty( i );
						attributes.remove( key );
					}
					else
					{
						config.getPropertyArray( i ).setStringValue( value );
						attributes.put( key, value );
					}
					break;
				}
			}
		}
		else if( !StringUtils.isNullOrEmpty( value ) )
		{
			PropertyConfig attr = config.addNewProperty();
			attr.setKey( key );
			attr.setStringValue( value );
			attributes.put( key, value );
		}
	}

	@Override
	public void removeAttribute( String key )
	{
		setAttribute( key, null );
	}

	@Override
	public Collection<String> getAttributes()
	{
		return attributes.keySet();
	}
}