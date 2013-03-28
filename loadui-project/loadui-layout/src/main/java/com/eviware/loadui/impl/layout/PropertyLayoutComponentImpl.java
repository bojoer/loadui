/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.layout;

import java.util.Map;

import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.util.MapUtils;
import com.google.common.collect.ImmutableMap;

public class PropertyLayoutComponentImpl<T> extends LayoutComponentImpl implements PropertyLayoutComponent<T>
{
	public final static String PROPERTY = "property";
	public final static String LABEL = "label";
	public final static String READ_ONLY = "readOnly";
	public final static String HINT = "hint";
	public final static String WIDGET = "widget";

	public PropertyLayoutComponentImpl( Map<String, ?> args )
	{
		super( args );

		if( !( properties.get( PROPERTY ) instanceof Property<?> ) )
			throw new IllegalArgumentException( "Illegal arguments: " + args );
	}

	public PropertyLayoutComponentImpl( Property<T> property, String label, String constraints, boolean readOnly,
			String hint )
	{
		this( ImmutableMap.<String, Object> builder().put( PROPERTY, property ).put( LABEL, label )
				.put( CONSTRAINTS, constraints ).put( READ_ONLY, readOnly ).put( HINT, ( hint == null ) ? "" : hint )
				.build() );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public Property<T> getProperty()
	{
		return ( Property<T> )properties.get( PROPERTY );
	}

	@Override
	public boolean isReadOnly()
	{
		return MapUtils.getOr( properties, READ_ONLY, false );
	}

	@Override
	public String getLabel()
	{
		return MapUtils.getOr( properties, LABEL, "" );
	}

	@Override
	public String getHint()
	{
		return MapUtils.getOr( properties, HINT, "" );
	}
}
