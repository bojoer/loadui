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
package com.eviware.loadui.util.serialization;

import java.util.Set;

import com.eviware.loadui.api.serialization.TypeSerializer;

@Deprecated
public class SerializationRegistry
{
	private final Set<TypeSerializer<?>> serializers;

	public SerializationRegistry( Set<TypeSerializer<?>> serializers )
	{
		this.serializers = serializers;
	}

	@SuppressWarnings( "unchecked" )
	public String serialize( Object value, boolean marshal )
	{
		TypeSerializer serializer = getSerializer( value.getClass() );
		return serializer.serialize( value, marshal );
	}

	public <T> String serialize( Class<T> type, T value, boolean marshal )
	{
		TypeSerializer<T> serializer = getSerializer( type );
		if( serializer == null )
			throw new RuntimeException( "No TypeSerializer for class '" + type + "' available!" );

		return serializer.serialize( value, marshal );
	}

	@SuppressWarnings( "unchecked" )
	public <T> TypeSerializer<T> getSerializer( Class<T> type )
	{
		for( TypeSerializer<?> serializer : serializers )
		{
			if( serializer.getType().equals( type ) )
				return ( TypeSerializer<T> )serializer;
		}
		return null;
	}

	public TypeSerializer<?> getSerializer( String type )
	{
		for( TypeSerializer<?> serializer : serializers )
		{
			if( serializer.getType().getName().equals( type ) )
				return serializer;
		}
		return null;
	}
}
