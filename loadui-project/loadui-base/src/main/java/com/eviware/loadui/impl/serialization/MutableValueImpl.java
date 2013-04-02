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
package com.eviware.loadui.impl.serialization;

import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.serialization.MutableValue;

public class MutableValueImpl<T> implements MutableValue<T>
{
	protected final ConversionService conversionService;
	private final Class<T> type;
	private T value;

	public MutableValueImpl( Class<T> type, Object value, ConversionService conversionService )
	{
		this.conversionService = conversionService;
		this.type = type;
		this.value = type.isInstance( value ) ? type.cast( value ) : conversionService.convert( value, type );
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

	@Override
	public void setValue( Object value )
	{
		this.value = type.isInstance( value ) ? type.cast( value ) : conversionService.convert( value, type );
	}

	@Override
	public String toString()
	{
		return "MutableValue(" + value + ")";
	}
}
