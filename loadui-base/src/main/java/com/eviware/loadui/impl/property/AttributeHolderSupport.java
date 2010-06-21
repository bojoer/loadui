package com.eviware.loadui.impl.property;

import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.config.PropertyConfig;
import com.eviware.loadui.config.PropertyListConfig;

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
					config.getPropertyArray( i ).setStringValue( value );
					break;
				}
			}
		}
		else
		{
			PropertyConfig attr = config.addNewProperty();
			attr.setKey( key );
			attr.setStringValue( value );
		}
		attributes.put( key, value );
	}

}
