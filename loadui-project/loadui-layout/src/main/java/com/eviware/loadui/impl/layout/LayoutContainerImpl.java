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
package com.eviware.loadui.impl.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.util.MapUtils;
import com.google.common.collect.ImmutableMap;

public class LayoutContainerImpl extends LayoutComponentImpl implements LayoutContainer
{
	public final static String LAYOUT_CONSTRAINTS = "layout";
	public final static String COLUMN_CONSTRAINTS = "column";
	public final static String ROW_CONSTRAINTS = "row";

	private boolean frozen;
	private List<LayoutComponent> components = new ArrayList<LayoutComponent>();

	public LayoutContainerImpl( Map<String, ?> args )
	{
		super( args );
	}

	public LayoutContainerImpl( String layoutConstraints, String colConstraints, String rowConstraints,
			String constraints )
	{
		this( ImmutableMap.of( LAYOUT_CONSTRAINTS, layoutConstraints, COLUMN_CONSTRAINTS, colConstraints,
				ROW_CONSTRAINTS, rowConstraints, CONSTRAINTS, constraints ) );
	}

	@Override
	public String getLayoutConstraints()
	{
		return MapUtils.getOr( properties, LAYOUT_CONSTRAINTS, "gap 10 0" );
	}

	@Override
	public String getColumnConstraints()
	{
		return MapUtils.getOr( properties, COLUMN_CONSTRAINTS, "" );
	}

	@Override
	public String getRowConstraints()
	{
		return MapUtils.getOr( properties, ROW_CONSTRAINTS, "align top" );
	}

	@Override
	public String getConstraints()
	{
		return MapUtils.getOr( properties, CONSTRAINTS, "" );
	}

	@Override
	public void freeze()
	{
		if( !frozen )
		{
			frozen = true;
			components = Collections.unmodifiableList( components );
		}
	}

	@Override
	public boolean isFrozen()
	{
		return frozen;
	}

	@Override
	public void add( int index, LayoutComponent element )
	{
		components.add( index, element );
	}

	@Override
	public boolean add( LayoutComponent e )
	{
		return components.add( e );
	}

	@Override
	public boolean addAll( Collection<? extends LayoutComponent> c )
	{
		return components.addAll( c );
	}

	@Override
	public boolean addAll( int index, Collection<? extends LayoutComponent> c )
	{
		return components.addAll( index, c );
	}

	@Override
	public void clear()
	{
		components.clear();
	}

	@Override
	public boolean contains( Object o )
	{
		return components.contains( o );
	}

	@Override
	public boolean containsAll( Collection<?> c )
	{
		return components.containsAll( c );
	}

	@Override
	public LayoutComponent get( int index )
	{
		return components.get( index );
	}

	@Override
	public int indexOf( Object o )
	{
		return components.indexOf( o );
	}

	@Override
	public boolean isEmpty()
	{
		return components.isEmpty();
	}

	@Override
	public Iterator<LayoutComponent> iterator()
	{
		return components.iterator();
	}

	@Override
	public int lastIndexOf( Object o )
	{
		return components.lastIndexOf( o );
	}

	@Override
	public ListIterator<LayoutComponent> listIterator()
	{
		return components.listIterator();
	}

	@Override
	public ListIterator<LayoutComponent> listIterator( int index )
	{
		return components.listIterator( index );
	}

	@Override
	public LayoutComponent remove( int index )
	{
		return components.remove( index );
	}

	@Override
	public boolean remove( Object o )
	{
		return components.remove( o );
	}

	@Override
	public boolean removeAll( Collection<?> c )
	{
		return components.removeAll( c );
	}

	@Override
	public boolean retainAll( Collection<?> c )
	{
		return components.retainAll( c );
	}

	@Override
	public LayoutComponent set( int index, LayoutComponent element )
	{
		return components.set( index, element );
	}

	@Override
	public int size()
	{
		return components.size();
	}

	@Override
	public List<LayoutComponent> subList( int fromIndex, int toIndex )
	{
		return components.subList( fromIndex, toIndex );
	}

	@Override
	public Object[] toArray()
	{
		return components.toArray();
	}

	@Override
	public <T> T[] toArray( T[] a )
	{
		return components.toArray( a );
	}
}
