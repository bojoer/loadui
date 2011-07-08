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
package com.eviware.loadui.impl.property;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.PropertyHolder;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.config.PropertyConfig;
import com.eviware.loadui.impl.serialization.MutableValueImpl;
import com.eviware.loadui.util.StringUtils;

public class PropertyImpl<T> extends MutableValueImpl<T> implements Property<T>
{
	private final PropertyConfig config;
	private final PropertyHolder owner;
	private boolean propagates;

	public PropertyImpl( PropertyHolder owner, PropertyConfig config, Class<T> type, ConversionService conversionService )
	{
		super( type, config.getStringValue() == null ? null : StringUtils.fixLineSeparators( config.getStringValue() ),
				conversionService );
		config.setType( type.getName() );
		this.owner = owner;
		this.config = config;
		this.propagates = config.getPropagates();
	}

	@Override
	public String getKey()
	{
		return config.getKey();
	}

	@Override
	public PropertyHolder getOwner()
	{
		return owner;
	}

	@Override
	public void setValue( Object value )
	{
		T oldVal = getValue();
		super.setValue( value );
		T newVal = getValue();
		if( ( oldVal == null && newVal == null ) || ( newVal != null && newVal.equals( oldVal ) ) )
			return;

		config.setStringValue( getStringValue() );
		owner.fireEvent( new PropertyEvent( owner, this, PropertyEvent.Event.VALUE, oldVal ) );
	}

	@Override
	public String toString()
	{
		return "Property[" + getKey() + "=" + getValue() + "]";
	}

	@Override
	public String getStringValue()
	{
		return StringUtils.fixLineSeparators( ( String )conversionService.convert( getValue(),
				TypeDescriptor.valueOf( getType() ), TypeDescriptor.valueOf( String.class ) ) );
	}

	@Override
	public boolean isPropagated()
	{
		return propagates;
	}

	public void makeNonPropagating()
	{
		if( propagates )
		{
			propagates = false;
			config.setPropagates( false );
		}
	}
}
