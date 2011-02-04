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

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.property.PropertyMap;
import com.eviware.loadui.config.PropertyConfig;
import com.eviware.loadui.config.PropertyListConfig;
import com.eviware.loadui.impl.model.ModelItemImpl;

public class PropertyMapImpl extends HashMap<String, Property<?>> implements PropertyMap
{
	private static final long serialVersionUID = 4955105240269868315L;

	private final ModelItemImpl<?> owner;
	private final PropertyListConfig config;
	private final ConversionService conversionService;

	public PropertyMapImpl( ModelItemImpl<?> owner, ConversionService conversionService )
	{
		this.owner = owner;
		this.conversionService = conversionService;
		config = owner.getConfig().getProperties() != null ? owner.getConfig().getProperties() : owner.getConfig()
				.addNewProperties();

		for( PropertyConfig pc : config.getPropertyArray() )
		{
			try
			{
				put( pc.getKey(), loadProperty( pc, Class.forName( pc.getType() ) ) );
			}
			catch( ClassNotFoundException e )
			{
				e.printStackTrace();
			}
		}
	}

	private <T> PropertyImpl<T> loadProperty( PropertyConfig pc, Class<T> type )
	{
		return new PropertyImpl<T>( owner, pc, type, conversionService );
	}

	@Override
	public void renameProperty( String key, String newKey )
	{
		if( containsKey( key ) && !containsKey( newKey ) )
		{
			put( newKey, remove( key ) );
			for( PropertyConfig p : config.getPropertyArray() )
			{
				if( p.getKey().equals( key ) )
				{
					p.setKey( newKey );
					owner.firePropertyEvent( get( newKey ), PropertyEvent.Event.RENAMED, key );
					return;
				}
			}
		}
		else
			throw new RuntimeException( "Cannot rename property '" + key + "': "
					+ ( !containsKey( key ) ? "Property does not exist!" : "Another property already has that name!" ) );
	}

	@Override
	public void clear()
	{
		super.clear();
		for( int i = config.getPropertyArray().length - 1; i >= 0; i-- )
			config.removeProperty( i );
	}

	@Override
	public Property<?> put( String key, Property<?> value )
	{
		return super.put( key, value );
	}

	@Override
	public void putAll( Map<? extends String, ? extends Property<?>> m )
	{
		for( Entry<? extends String, ? extends Property<?>> entry : m.entrySet() )
			put( entry.getKey(), entry.getValue() );
	}

	@Override
	public Property<?> remove( Object key )
	{
		Property<?> property = super.remove( key );
		if( property != null )
			owner.firePropertyEvent( property, PropertyEvent.Event.DELETED, property.getValue() );
		return property;
	}

	@Override
	public <T> Property<T> createProperty( String key, Class<T> type )
	{
		return createProperty( key, type, null );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T> Property<T> createProperty( String key, Class<T> type, Object initialValue )
	{
		if( containsKey( key ) && get( key ).getType() == type )
			return ( Property<T> )get( key );

		PropertyConfig pc = config.addNewProperty();
		pc.setKey( key );
		pc.setStringValue( conversionService.convert( conversionService.convert( initialValue, type ), String.class ) );
		PropertyImpl<T> property = loadProperty( pc, type );
		put( key, property );
		owner.firePropertyEvent( property, PropertyEvent.Event.CREATED, null );
		return property;
	}
}
