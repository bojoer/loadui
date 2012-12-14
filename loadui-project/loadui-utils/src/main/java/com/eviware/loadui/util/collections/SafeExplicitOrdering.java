package com.eviware.loadui.util.collections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * Like Ordering.explicit, only it does not throw an exception when an item is
 * compared that does not exist in the ordering. Any items not contained in the
 * explicit ordering appear after the contained items. This ordering does not
 * order the remaining items at all, returning 0 in the compare method.
 * 
 * @author dain.nilsson
 * 
 * @param <T>
 */
public class SafeExplicitOrdering<T> implements Comparator<T>
{
	public static <T> Ordering<T> of( T[] valuesInOrder )
	{
		return of( Arrays.asList( valuesInOrder ) );
	}

	@SafeVarargs
	public static <T> Ordering<T> of( T leastValue, T... remainingValuesInOrder )
	{
		return of( Lists.asList( leastValue, remainingValuesInOrder ) );
	}

	public static <T> Ordering<T> of( List<T> valuesInOrder )
	{
		return Ordering.from( new SafeExplicitOrdering<>( valuesInOrder ) );
	}

	private final List<T> itemsInOrder;
	private final Ordering<T> explicit;

	private SafeExplicitOrdering( List<T> items )
	{
		itemsInOrder = ImmutableList.copyOf( items );
		explicit = Ordering.explicit( items );
	}

	@Override
	public int compare( T o1, T o2 )
	{
		try
		{
			return explicit.compare( o1, o2 );
		}
		catch( ClassCastException e )
		{
			int o1Contained = itemsInOrder.contains( o1 ) ? 1 : 0;
			int o2Contained = itemsInOrder.contains( o2 ) ? 1 : 0;

			return o2Contained - o1Contained;
		}
	}
}
