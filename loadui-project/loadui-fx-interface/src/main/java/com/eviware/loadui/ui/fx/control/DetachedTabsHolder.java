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
package com.eviware.loadui.ui.fx.control;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.scene.layout.StackPane;
import javafx.util.Callback;

/**
 * Keeps references to detached tabs so they can be reached by other parts of
 * the code.
 * 
 * @author renato
 * 
 */
public class DetachedTabsHolder
{

	private static DetachedTabsHolder INSTANCE;
	private static final AtomicInteger idGenerator = new AtomicInteger();

	private final Map<Integer, StackPane> tabRefsById = new HashMap<>();
	private final Deque<Callback<StackPane, Boolean>> onDetachCallbacks = new ArrayDeque<>();
	private final Deque<Callback<StackPane, Boolean>> onReattachCallbacks = new ArrayDeque<>();

	private DetachedTabsHolder()
	{
	} // singleton

	/**
	 * @return singleton instance of this
	 */
	public synchronized static DetachedTabsHolder get()
	{
		if( INSTANCE == null )
			INSTANCE = new DetachedTabsHolder();
		return INSTANCE;
	}

	/**
	 * 
	 * @param tabContents
	 * @return id of this tabContents which can be used later to remove it
	 */
	int add( StackPane tabContents )
	{
		callSynchronized( onDetachCallbacks, tabContents );
		int id = idGenerator.incrementAndGet();
		tabRefsById.put( id, tabContents );
		return id;
	}

	boolean remove( int id )
	{
		StackPane detachedTabContainer = tabRefsById.remove( id );
		callSynchronized( onReattachCallbacks, detachedTabContainer );
		return detachedTabContainer != null;
	}

	/**
	 * @return true if there is no tab currently detached, false otherwise
	 */
	public boolean isEmpty()
	{
		return tabRefsById.isEmpty();
	}

	/**
	 * Add the given callback to listen on tabs being detached from the main
	 * Stage. If the callback returns false, it will be removed automatically,
	 * ensuring the callback is called only once.
	 * 
	 * @param callback
	 */
	public void addOnDetachCallback( Callback<StackPane, Boolean> callback )
	{
		synchronized( onDetachCallbacks )
		{
			onDetachCallbacks.add( callback );
		}
	}

	/**
	 * Add the given callback to listen on tabs being re-attached to the main
	 * Stage. If the callback returns false, it will be removed automatically,
	 * ensuring the callback is called only once.
	 * 
	 * @param callback
	 */
	public void addOnReattachCallback( Callback<StackPane, Boolean> callback )
	{
		synchronized( onReattachCallbacks )
		{
			onReattachCallbacks.add( callback );
		}
	}

	private static void callSynchronized( Deque<Callback<StackPane, Boolean>> queue, StackPane tabContents )
	{
		Deque<Callback<StackPane, Boolean>> toRemove = new ArrayDeque<>();
		synchronized( queue )
		{
			for( Callback<StackPane, Boolean> callback : queue )
			{
				Boolean keep = callback.call( tabContents );
				if( keep != Boolean.TRUE )
					toRemove.add( callback );
			}
			for( Callback<StackPane, Boolean> callback : toRemove )
				queue.remove( callback );
		}
	}

}
