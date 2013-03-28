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
package com.eviware.loadui.impl.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.PropertyHolder;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.property.PropertyMap;
import com.eviware.loadui.config.PropertyConfig;
import com.eviware.loadui.config.PropertyListConfig;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class PropertyMapImpl implements PropertyMap
{
	public static final Logger log = LoggerFactory.getLogger( PropertyMapImpl.class );
	private final Map<String, Property<?>> map = new HashMap<>();
	private final Multimap<String, PropertyConfig> notLoadedProperties = HashMultimap.create();

	private final PropertyHolder owner;
	private final PropertyListConfig config;
	private final ConversionService conversionService;

	public PropertyMapImpl( PropertyHolder owner, ConversionService conversionService, PropertyListConfig config )
	{
		this.owner = owner;
		this.config = config;
		this.conversionService = conversionService;

		for( PropertyConfig pc : config.getPropertyList() )
		{
			try
			{
				put( pc.getKey(), loadProperty( pc, Class.forName( pc.getType() ) ) );
			}
			catch( ClassNotFoundException e )
			{
				log.debug( "Unable to load Property {} of type {}, class not found.", pc.getKey(), pc.getType() );
				notLoadedProperties.put( pc.getType(), pc );
			}
		}
	}

	private <T> PropertyImpl<T> loadProperty( PropertyConfig pc, Class<T> type )
	{
		return new PropertyImpl<>( owner, pc, type, conversionService );
	}

	private void firePropertyEvent( Property<?> property, PropertyEvent.Event event, Object argument )
	{
		owner.fireEvent( new PropertyEvent( owner, property, event, argument ) );
	}

	@Override
	public void renameProperty( String key, String newKey )
	{
		if( containsKey( key ) && !containsKey( newKey ) )
		{
			put( newKey, remove( key ) );
			for( PropertyConfig p : config.getPropertyList() )
			{
				if( p.getKey().equals( key ) )
				{
					p.setKey( newKey );
					firePropertyEvent( get( newKey ), PropertyEvent.Event.RENAMED, key );
					return;
				}
			}
		}
		else
		{
			throw new RuntimeException( "Cannot rename property '" + key + "': "
					+ ( !containsKey( key ) ? "Property does not exist!" : "Another property already has that name!" ) );
		}
	}

	@Override
	public void clear()
	{
		map.clear();
		for( int i = config.sizeOfPropertyArray() - 1; i >= 0; i-- )
		{
			config.removeProperty( i );
		}
	}

	@Override
	public final Property<?> put( String key, Property<?> value )
	{
		return map.put( key, value );
	}

	@Override
	public void putAll( Map<? extends String, ? extends Property<?>> m )
	{
		for( Entry<? extends String, ? extends Property<?>> entry : m.entrySet() )
		{
			put( entry.getKey(), entry.getValue() );
		}
	}

	@Override
	public Property<?> remove( Object key )
	{
		Property<?> property = map.remove( key );
		if( property != null )
		{
			firePropertyEvent( property, PropertyEvent.Event.DELETED, property.getValue() );
		}
		return property;
	}

	@Override
	public <T> Property<T> createProperty( String key, Class<T> type )
	{
		return createProperty( key, type, null, true );
	}

	@Override
	public <T> Property<T> createProperty( String key, Class<T> type, Object initialValue )
	{
		return createProperty( key, type, initialValue, true );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T> Property<T> createProperty( String key, Class<T> type, Object initialValue, boolean propagates )
	{
		for( PropertyConfig propertyConfig : notLoadedProperties.removeAll( type.getName() ) )
		{
			loadProperty( propertyConfig, type );
		}

		PropertyImpl<T> property = ( PropertyImpl<T> )get( key );
		if( property != null )
		{
			if( property.getType() == type )
			{
				if( !propagates )
					property.makeNonPropagating();
			}
			else
			{
				remove( key );
			}

			return property;
		}

		PropertyConfig pc = config.addNewProperty();
		pc.setPropagates( propagates );
		pc.setKey( key );
		pc.setStringValue( conversionService.convert( conversionService.convert( initialValue, type ), String.class ) );
		property = loadProperty( pc, type );
		put( key, property );
		firePropertyEvent( property, PropertyEvent.Event.CREATED, null );

		return property;
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public Property<?> get( Object key )
	{
		return map.get( key );
	}

	@Override
	public boolean containsKey( Object key )
	{
		return map.containsKey( key );
	}

	@Override
	public boolean containsValue( Object value )
	{
		return map.containsValue( value );
	}

	@Override
	public Set<String> keySet()
	{
		return map.keySet();
	}

	@Override
	public Collection<Property<?>> values()
	{
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Property<?>>> entrySet()
	{
		return map.entrySet();
	}
}
