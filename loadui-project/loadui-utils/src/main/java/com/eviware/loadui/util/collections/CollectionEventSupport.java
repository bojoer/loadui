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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.ReleasableUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * Support class for adding and removing objects to a collection, taking care of
 * firing CollectionEvents when needed. It also allows attaching an arbitrary
 * Object to each item. If you are not interested in this feature, it is
 * recommended that you set the second Generic parameter to Void.
 * 
 * @author dain.nilsson
 * 
 * @param <V>
 */
public class CollectionEventSupport<V, A> implements Releasable
{
	/**
	 * Creates a new CollectionEventSupport using the given parameters. Same as
	 * using the constructor, but uses type inference for Generics.
	 * 
	 * @param owner
	 * @param collectionKey
	 * @return
	 */
	public static <X, Y> CollectionEventSupport<X, Y> of( @Nonnull EventFirer owner, @Nonnull String collectionKey )
	{
		return new CollectionEventSupport<X, Y>( owner, collectionKey );
	}

	private final static Object DUMMY = new Object();

	private final Map<V, Object> values = Maps.newHashMap();

	private final EventFirer owner;
	private final String collectionKey;

	public CollectionEventSupport( @Nonnull EventFirer owner, @Nonnull String collectionKey )
	{
		this.owner = owner;
		this.collectionKey = collectionKey;
	}

	/**
	 * Returns an immutable copy of the contained values.
	 * 
	 * @return
	 */
	@Nonnull
	public Collection<V> getItems()
	{
		return ImmutableSet.copyOf( values.keySet() );
	}

	/**
	 * Checks if the given item is in the collection.
	 * 
	 * @param item
	 * @return
	 */
	public boolean containsItem( @Nonnull V item )
	{
		return values.containsKey( Preconditions.checkNotNull( item ) );
	}

	/**
	 * Adds an item to the collection, and fires an ADDED event, unless the
	 * collection already contains the item.
	 * 
	 * @param item
	 * @return True if the item was added, false if it was already contained.
	 */
	public boolean addItem( @Nonnull V item )
	{
		return doAddItemWith( item, null, null );
	}

	/**
	 * Adds an item to the collection, and fires an ADDED event, unless the
	 * collection already contains the item. Before firing the event, runs the
	 * given Runnable iff the item was added.
	 * 
	 * @param item
	 * @param onAdd
	 * @return
	 */
	public boolean addItem( @Nonnull V item, @Nullable Runnable onAdd )
	{
		return doAddItemWith( item, onAdd, null );
	}

	/**
	 * Adds an item to the collection as with {@link addItem}, but also attaches
	 * an Object to the item reference, which can be retrieved using
	 * {@link getAttachment}. This attachment may hold arbitrary data which
	 * should be associated to the item. If the item already exists in the
	 * collection the attachment will NOT be changed.
	 * 
	 * @see addItemWith( V item, Runnable onAdd, Object attachment )
	 * @param item
	 * @param attachment
	 * @return
	 */
	public boolean addItemWith( @Nonnull V item, @Nonnull A attachment )
	{
		return doAddItemWith( item, null, Preconditions.checkNotNull( attachment ) );
	}

	/**
	 * Adds an item to the collection as with {@link addItem}, but also attaches
	 * an Object to the item reference, which can be retrieved using
	 * {@link getAttachment}. This attachment may hold arbitrary data which
	 * should be associated to the item. If the item already exists in the
	 * collection the attachment will NOT be changed.
	 * 
	 * @see addItem
	 * @param item
	 * @param attachment
	 * @return
	 */
	public boolean addItemWith( @Nonnull V item, @Nullable Runnable onAdd, @Nonnull A attachment )
	{
		return doAddItemWith( item, onAdd, Preconditions.checkNotNull( attachment ) );
	}

	private boolean doAddItemWith( @Nonnull V item, @Nullable Runnable onAdd, @Nullable A attachment )
	{
		if( !values.containsKey( Preconditions.checkNotNull( item ) ) )
		{
			values.put( item, attachment == null ? DUMMY : attachment );
			if( onAdd != null )
			{
				onAdd.run();
			}

			owner.fireEvent( new CollectionEvent( owner, collectionKey, CollectionEvent.Event.ADDED, item ) );

			return true;
		}

		return false;
	}

	/**
	 * Gets the Object attached to a previously added item, or null if no
	 * attachment was added.
	 * 
	 * @param item
	 * @return
	 */
	public A getAttachment( @Nonnull V item )
	{
		@SuppressWarnings( "unchecked" )
		A attachment = ( A )values.get( Preconditions.checkNotNull( item ) );

		return attachment == DUMMY ? null : attachment;
	}

	/**
	 * Removes an item from the collection, and fires a REMOVED event, unless the
	 * item isn't contained by the collection.
	 * 
	 * @param item
	 * @return True if the item was removed, false if it wasn't in the collection
	 *         to begin with.
	 */
	public boolean removeItem( @Nonnull V item )
	{
		return removeItem( item, null );
	}

	/**
	 * Removes an item from the collection, and fires a REMOVED event, unless the
	 * item isn't contained by the collection. Before firing the event, runs the
	 * given Runnable iff the item was removed.
	 * 
	 * @param item
	 * @param onRemove
	 * @return
	 */
	public boolean removeItem( @Nonnull V item, @Nullable Runnable onRemove )
	{
		if( values.remove( Preconditions.checkNotNull( item ) ) != null )
		{
			if( onRemove != null )
			{
				onRemove.run();
			}
			owner.fireEvent( new CollectionEvent( owner, collectionKey, CollectionEvent.Event.REMOVED, item ) );

			return true;
		}

		return false;
	}

	/**
	 * Removes all items from the collection, firing REMOVED events for each of
	 * them.
	 * 
	 */
	public Set<V> removeAllItems()
	{
		ImmutableSet<V> removed = ImmutableSet.copyOf( values.keySet() );
		values.clear();

		for( V item : removed )
		{
			owner.fireEvent( new CollectionEvent( owner, collectionKey, CollectionEvent.Event.REMOVED, item ) );
		}

		return removed;
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( removeAllItems() );
	}
}
