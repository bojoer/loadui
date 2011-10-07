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
package com.eviware.loadui.impl.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.eviware.loadui.api.model.AttributeHolder;

/**
 * AttributeHolderSupport which delegates storing attributes to another
 * AttributeHolder, using a specified prefix for all attribute names.
 * 
 * @author dain.nilsson
 */
public class DelegatingAttributeHolderSupport
{
	public final AttributeHolder delegate;
	public final String prefix;
	public final int prefixLength;

	public DelegatingAttributeHolderSupport( AttributeHolder delegate, String prefix )
	{
		this.delegate = delegate;
		this.prefix = prefix;
		prefixLength = prefix.length();
	}

	public void setAttribute( String key, String value )
	{
		delegate.setAttribute( prefix + key, value );
	}

	public String getAttribute( String key, String defaultValue )
	{
		return delegate.getAttribute( prefix + key, defaultValue );
	}

	public void removeAttribute( String key )
	{
		delegate.removeAttribute( prefix + key );
	}

	public Collection<String> getAttributes()
	{
		List<String> attributes = new ArrayList<String>();
		for( String attribute : delegate.getAttributes() )
			if( attribute.startsWith( prefix ) )
				attributes.add( attribute.substring( prefixLength ) );

		return attributes;
	}
}
