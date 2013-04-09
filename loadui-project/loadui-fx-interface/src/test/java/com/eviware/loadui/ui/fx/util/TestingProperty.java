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
package com.eviware.loadui.ui.fx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.model.PropertyHolder;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.util.BeanInjector;

/**
 * A Property implementation designed for easy use of Properties in unit tests.
 * 
 * @author Henrik Olsson
 * @param <T>
 */

public class TestingProperty<T> implements Property<T>
{
	private final String key;
	private T value;
	private final Class<T> type;

	protected static final Logger log = LoggerFactory.getLogger( TestingProperty.class );

	public TestingProperty( Class<T> type, String key, Object value )
	{
		this.type = type;
		this.key = key;
		setValue( value );
	}

	@Override
	public PropertyHolder getOwner()
	{
		return null;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public String getStringValue()
	{
		return "Property[" + getKey() + "=" + getValue() + "]";
	}

	@Override
	public boolean isPropagated()
	{
		return false;
	}

	@Override
	public void setValue( Object value )
	{
		if( value == null )
			this.value = null;
		else
			this.value = type.isInstance( value ) ? type.cast( value ) : BeanInjector.getBean( ConversionService.class )
					.convert( value, type );
	}

	@Override
	public Class<T> getType()
	{
		return type;
	}

	@Override
	public T getValue()
	{
		return value;
	}
}
