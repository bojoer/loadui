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
package com.eviware.loadui.impl.layout;

import java.util.Map;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.util.MapUtils;

public class LayoutComponentImpl implements LayoutComponent
{
	public static final String CONSTRAINTS = "constraints";

	protected final Map<String, ?> properties;

	public LayoutComponentImpl( Map<String, ?> args )
	{
		properties = args;
	}

	public LayoutComponentImpl( String constraints )
	{
		this( MapUtils.build( String.class, Object.class ).put( CONSTRAINTS, constraints ).getImmutable() );
	}

	@Override
	public String getConstraints()
	{
		return MapUtils.getOr( properties, CONSTRAINTS, "" );
	}

	@Override
	public Object get( String key )
	{
		return properties.get( key );
	}

	@Override
	public boolean has( String key )
	{
		return properties.containsKey( key );
	}
}
