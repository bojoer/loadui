package com.eviware.loadui.impl.statistics.store.testevents;

import java.util.Arrays;

import com.eviware.loadui.api.traits.Labeled;
import com.google.common.base.Objects;

public final class TestEventSourceConfig implements Labeled
{
	private final String label;
	private final String typeName;
	private final byte[] data;

	public TestEventSourceConfig( String label, String typeName, byte[] data )
	{
		this.label = label;
		this.typeName = typeName;
		this.data = data;
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