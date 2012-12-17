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
package com.eviware.loadui.util.collections;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.RandomAccess;

/**
 * Fixed size List which is backed by an Array. When full, appending a new
 * element will drop the first element of the List. This implementation is
 * optimized for appending data to the end and provides fast random access for
 * reads.
 * 
 * @author dain.nilsson
 */
//This class is never used --Henrik
public class CircularList<E> extends AbstractList<E> implements RandomAccess, Cloneable
{
	private int size;
	private int capacity;
	private Object[] elementData;
	private int position = 0;

	public CircularList( int initialCapacity )
	{
		if( initialCapacity < 0 )
			throw new IllegalArgumentException( "" );

		capacity = initialCapacity;
		elementData = new Object[capacity];
	}

	public CircularList()
	{
		this( 10 );
	}

	public CircularList( Collection<? extends E> c )
	{
		elementData = c.toArray();
		capacity = elementData.length;
		size = capacity;
		if( elementData.getClass() != Object[].class )
			elementData = Arrays.copyOf( elementData, size, Object[].class );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public E get( int index )
	{
		return ( E )elementData[( position + index ) % capacity];
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public E set( int index, E element )
	{
		if( index > size )
			throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );
		if( index == size )
			size++ ;

		int realIndex = ( position + index ) % capacity;
		E oldVal = ( E )elementData[realIndex];
		elementData[realIndex] = element;

		return oldVal;
	}

	@Override
	public boolean add( E element )
	{
		if( size < capacity )
		{
			set( size, element );
		}
		else
		{
			position = ( position + 1 ) % capacity;
			set( size - 1, element );
		}

		return true;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public void add( int index, E element )
	{
		if( index == size )
		{
			add( element );
			return;
		}

		if( index >= size || index < 0 )
			throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );

		resetPosition();
		System.arraycopy( elementData, 1, elementData, 0, index );
		set( index, element );
	}

	@Override
	public void clear()
	{
		size = 0;
		position = 0;
	}

	@Override
	public E remove( int index )
	{
		if( index >= size || index < 0 )
			throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + size );

		resetPosition();
		E oldVal = get( index );
		System.arraycopy( elementData, index + 1, elementData, index, size - index );

		return oldVal;
	}

	@Override
	public Object[] toArray()
	{
		resetPosition();

		return Arrays.copyOf( elementData, size );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T> T[] toArray( T[] a )
	{
		resetPosition();

		if( a.length < size )
			return ( T[] )Arrays.copyOf( elementData, size, a.getClass() );
		System.arraycopy( elementData, 0, a, 0, size );
		if( a.length > size )
			a[size] = null;
		return a;
	}

	public void setCapacity( int newCapacity )
	{
		if( this.capacity == newCapacity )
			return;

		resetPosition();

		Object[] newObjectData = new Object[newCapacity];
		int offset = size > newCapacity ? size - newCapacity : 0;
		size -= offset;
		System.arraycopy( elementData, offset, newObjectData, 0, size );

		elementData = newObjectData;
		capacity = newCapacity;
	}

	public int getCapacity()
	{
		return capacity;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		try
		{
			@SuppressWarnings( "unchecked" )
			CircularList<E> clone = ( CircularList<E> )super.clone();
			clone.elementData = Arrays.copyOf( elementData, size );
			return clone;
		}
		catch( CloneNotSupportedException e )
		{
			throw new InternalError();
		}
	}

	private void resetPosition()
	{
		if( position == 0 )
			return;

		Object[] temp = new Object[position];
		System.arraycopy( elementData, 0, temp, 0, position );
		System.arraycopy( elementData, position, elementData, 0, size - position );
		System.arraycopy( temp, 0, elementData, size - position, position );
		position = 0;
	}
}
