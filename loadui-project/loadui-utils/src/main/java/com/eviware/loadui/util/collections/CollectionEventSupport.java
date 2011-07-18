package com.eviware.loadui.util.collections;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.ReleasableUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Support class for adding and removing objects to a collection, taking care of
 * firing CollectionEvents when needed.
 * 
 * @author dain.nilsson
 * 
 * @param <V>
 */
public class CollectionEventSupport<V> implements Releasable
{
	private final HashSet<V> values = Sets.newHashSet();

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
		return ImmutableSet.copyOf( values );
	}

	/**
	 * Checks if the given item is in the collection.
	 * 
	 * @param item
	 * @return
	 */
	public boolean containsItem( V item )
	{
		return values.contains( item );
	}

	/**
	 * Adds an item to the collection, and fires an ADDED event, unless the
	 * collection already contains the item.
	 * 
	 * @param item
	 * @return True if the item was added, false if it was already contained.
	 */
	public boolean addItem( V item )
	{
		return addItem( item, null );
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
	public boolean addItem( V item, @Nullable Runnable onAdd )
	{
		if( values.add( item ) )
		{
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
	 * Removes an item from the collection, and fires a REMOVED event, unless the
	 * item isn't contained by the collection.
	 * 
	 * @param item
	 * @return True if the item was removed, false if it wasn't in the collection
	 *         to begin with.
	 */
	public boolean removeItem( V item )
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
	public boolean removeItem( V item, Runnable onRemove )
	{
		if( values.remove( item ) )
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

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( values );
		values.clear();
	}
}
