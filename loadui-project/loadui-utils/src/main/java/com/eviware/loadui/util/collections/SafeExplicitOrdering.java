/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
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
