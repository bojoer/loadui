package com.eviware.loadui.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.model.OrderedCollection;
import com.eviware.loadui.util.ReleasableUtils;

public class OrderedCollectionSupport<ChildType>
{
	private final List<ChildType> children = new ArrayList<ChildType>();
	private final OrderedCollection<ChildType> owner;

	public OrderedCollectionSupport( OrderedCollection<ChildType> owner )
	{
		this.owner = owner;
	}

	public Collection<ChildType> getChildren()
	{
		return Collections.unmodifiableList( children );
	}

	public int getChildCount()
	{
		return children.size();
	}

	public int indexOf( ChildType child )
	{
		return children.indexOf( child );
	}

	public ChildType getChildAt( int index )
	{
		return children.get( index );
	}

	public void releaseChildren()
	{
		ReleasableUtils.releaseAll( children );
		children.clear();
	}

	public void moveChild( ChildType child, int index )
	{
		int oldIndex = children.indexOf( child );
		if( oldIndex == -1 )
			throw new IllegalArgumentException( "The object provided is not a child of this OrderedCollection" );
		if( oldIndex != index )
		{
			children.add( index, children.remove( oldIndex ) );
			owner.fireEvent( new BaseEvent( owner, OrderedCollection.CHILD_ORDER ) );
		}
	}

	public void addChild( ChildType child )
	{
		if( children.contains( child ) )
			throw new IllegalArgumentException( "Object " + child + " is already a member of " + owner );
		children.add( child );
		owner.fireEvent( new CollectionEvent( owner, OrderedCollection.CHILDREN, CollectionEvent.Event.ADDED, child ) );
	}

	public void removeChild( ChildType child )
	{
		if( children.remove( child ) )
			owner.fireEvent( new CollectionEvent( owner, OrderedCollection.CHILDREN, CollectionEvent.Event.REMOVED, child ) );
	}
}