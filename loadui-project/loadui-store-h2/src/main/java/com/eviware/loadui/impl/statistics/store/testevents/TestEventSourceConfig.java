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
package com.eviware.loadui.impl.statistics.store.testevents;

import java.util.Arrays;

import com.eviware.loadui.api.traits.Labeled;
import com.google.common.base.Objects;

public final class TestEventSourceConfig implements Labeled
{
	private final Long id;
	private final String label;
	private final String typeName;
	private final String hash;
	private final byte[] data;

	public TestEventSourceConfig( String label, String typeName, byte[] data, String hash, Long id )
	{
		this.label = label;
		this.typeName = typeName;
		this.data = data;
		this.hash = hash;
		this.id = id;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	public String getTypeName()
	{
		return typeName;
	}

	public byte[] getData()
	{
		return data;
	}

	public String getHash()
	{
		return hash;
	}

	public Long getId()
	{
		return id;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode( typeName, label, data );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		TestEventSourceConfig other = ( TestEventSourceConfig )obj;
		if( !Arrays.equals( data, other.data ) )
			return false;
		if( label == null )
		{
			if( other.label != null )
				return false;
		}
		else if( !label.equals( other.label ) )
			return false;
		if( typeName == null )
		{
			if( other.typeName != null )
				return false;
		}
		else if( !typeName.equals( other.typeName ) )
			return false;
		return true;
	}

}