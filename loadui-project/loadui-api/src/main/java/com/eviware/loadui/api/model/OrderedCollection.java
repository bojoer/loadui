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
package com.eviware.loadui.api.model;

import java.util.Collection;

import com.eviware.loadui.api.events.EventFirer;

/**
 * Object which holds a number of child nodes, which are ordered.
 * 
 * @author dain.nilsson
 * 
 * @param <ChildType>
 */
public interface OrderedCollection<ChildType> extends EventFirer
{
	// CollectionEvent fired when a child is added or removed.
	public static final String CHILDREN = OrderedCollection.class.getName() + "@children";

	// BaseEvent fired when the order of the contained children changes.
	public static final String CHILD_ORDER = OrderedCollection.class.getName() + "@childOrder";

	/**
	 * Gets an ordered Collection of all contained children.
	 * 
	 * @return
	 */
	public Collection<ChildType> getChildren();

	/**
	 * Gets the number of contained children.
	 * 
	 * @return
	 */
	public int getChildCount();

	/**
	 * Gets the positional index of a given child.
	 * 
	 * @param child
	 * @return
	 */
	public int indexOf( ChildType child );

	/**
	 * Gets the child at the given index.
	 * 
	 * @param index
	 * @return
	 */
	public ChildType getChildAt( int index );

	/**
	 * Mutable version of OrderedCollection, which allows adding, removing and
	 * reordering of children.
	 * 
	 * @author dain.nilsson
	 * 
	 * @param <ChildType>
	 */
	public interface Mutable<ChildType> extends OrderedCollection<ChildType>
	{
		/**
		 * Moved a contained child to a new position, as defined by the given
		 * positional index.
		 * 
		 * @param child
		 * @param index
		 */
		public void moveChild( ChildType child, int index );

		/**
		 * Appends a new child to the end of the collection.
		 * 
		 * @param child
		 */
		public void addChild( ChildType child );

		/**
		 * Removes a child from the OrderedCollection.
		 * 
		 * @param child
		 */
		public void removeChild( ChildType child );
	}
}
