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
package com.eviware.loadui.impl.terminal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.serialization.MutableValue;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.serialization.MutableValueImpl;

public class TerminalMessageImpl implements TerminalMessage
{
	private final Map<String, MutableValue<?>> values = new HashMap<String, MutableValue<?>>();
	private final ConversionService conversionService;

	public TerminalMessageImpl( ConversionService conversionService )
	{
		this.conversionService = conversionService;
	}

	@Override
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public TerminalMessage copy()
	{
		TerminalMessageImpl cpy = new TerminalMessageImpl( conversionService );
		synchronized( values )
		{
			for( Entry<String, MutableValue<?>> entry : values.entrySet() )
				cpy.values.put( entry.getKey(), new MutableValueImpl( entry.getValue().getType(), entry.getValue()
						.getValue(), conversionService ) );
		}

		return cpy;
	}

	@Override
	public Object get( Object key )
	{
		return values.containsKey( key ) ? values.get( key ).getValue() : null;
	}

	@Override
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public Object put( String key, Object value )
	{
		MutableValue<?> oldVal = values.get( key );
		Object old = oldVal == null ? null : oldVal.getValue();

		if( oldVal != null && oldVal.getType().isInstance( value ) )
			oldVal.setValue( value );
		else
		{
			synchronized( values )
			{
				values.put( key, new MutableValueImpl( value == null ? Object.class : value.getClass(), value,
						conversionService ) );
			}
		}
		return old;
	}

	@Override
	public <T> void put( String key, T value, Class<T> type )
	{
		synchronized( values )
		{
			values.put( key, new MutableValueImpl<T>( type, value, conversionService ) );
		}
	}

	@Override
	public Object serialize()
	{
		Map<String, String[]> serialized = new HashMap<String, String[]>();
		for( Entry<String, MutableValue<?>> entry : values.entrySet() )
		{
			MutableValue<?> value = entry.getValue();
			String[] parts;
			try
			{
				parts = new String[] { conversionService.convert( value.getValue(), String.class ),
						value.getType().getName() };
			}
			catch( Exception e )
			{
				parts = new String[] { String.valueOf( value.getValue() ), String.class.getName() };
			}

			serialized.put( entry.getKey(), parts );
		}

		return serialized;
	}

	@Override
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public void load( Object serialized )
	{
		if( !( serialized instanceof Map<?, ?> ) )
			throw new IllegalArgumentException( "" );

		Map<String, Object[]> data = ( Map<String, Object[]> )serialized;
		for( Entry<String, Object[]> entry : data.entrySet() )
		{
			Object[] args = entry.getValue();
			try
			{
				Class<?> type = Class.forName( ( String )args[1] );
				synchronized( values )
				{
					values.put( entry.getKey(), new MutableValueImpl( type, args[0], conversionService ) );
				}
			}
			catch( ClassNotFoundException e )
			{
				throw new RuntimeException( e );
			}
		}
	}

	@Override
	public Set<Entry<String, Object>> entrySet()
	{
		Set<Entry<String, Object>> entrySet = new HashSet<Entry<String, Object>>();
		for( String key : keySet() )
			entrySet.add( new InternalEntry( key ) );

		return entrySet;
	}

	@Override
	public void clear()
	{
		synchronized( values )
		{
			values.clear();
		}
	}

	@Override
	public boolean containsKey( Object key )
	{
		return values.containsKey( key );
	}

	@Override
	public boolean containsValue( Object value )
	{
		if( value != null )
			for( MutableValue<?> v : values.values() )
				if( value.equals( v.getValue() ) )
					return true;

		return false;
	}

	@Override
	public boolean isEmpty()
	{
		return values.isEmpty();
	}

	@Override
	public Set<String> keySet()
	{
		return values.keySet();
	}

	@Override
	public void putAll( Map<? extends String, ? extends Object> m )
	{
		for( Entry<? extends String, ? extends Object> entry : m.entrySet() )
			put( entry.getKey(), entry.getValue() );
	}

	@Override
	public Object remove( Object key )
	{
		MutableValue<?> value = values.remove( key );
		return value == null ? null : value.getValue();
	}

	@Override
	public int size()
	{
		return values.size();
	}

	@Override
	public Collection<Object> values()
	{
		Collection<Object> objects = new ArrayList<Object>();
		for( MutableValue<?> value : values.values() )
			objects.add( value.getValue() );

		return objects;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + values.toString();
	}

	private class InternalEntry implements Map.Entry<String, Object>
	{
		private final String key;

		private InternalEntry( String key )
		{
			this.key = key;
		}

		@Override
		public String getKey()
		{
			return key;
		}

		@Override
		public Object getValue()
		{
			return get( key );
		}

		@Override
		public Object setValue( Object value )
		{
			return put( getKey(), value );
		}
	}
}
