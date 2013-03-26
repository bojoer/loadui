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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Wraps a collection of Futures in a single Future, allowing to wait for them
 * all to complete, and returning their individual values in a List.
 * 
 * @author dain.nilsson
 * 
 * @param <V>
 */
public class CollectionFuture<V> implements Future<List<V>>
{
	private final ReentrantLock lock = new ReentrantLock();
	private final Iterable<? extends Future<? extends V>> futures;
	private List<V> results;
	private boolean cancelled = false;

	public CollectionFuture( Iterable<? extends Future<? extends V>> futures )
	{
		this.futures = futures;
	}

	public int size()
	{
		return Iterables.size( futures );
	}

	@Override
	public boolean cancel( boolean mayInterruptIfRunning )
	{
		boolean success = true;
		for( Future<? extends V> future : futures )
		{
			if( !future.cancel( mayInterruptIfRunning ) )
				success = false;
		}
		cancelled = true;
		return success;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public boolean isDone()
	{
		return cancelled || results != null;
	}

	@Override
	public List<V> get() throws InterruptedException, ExecutionException
	{
		lock.lock();
		try
		{
			if( results == null )
			{
				ArrayList<V> resultList = Lists.newArrayList();
				for( Future<? extends V> future : futures )
				{
					resultList.add( future.get() );
				}

				results = resultList;
			}
		}
		finally
		{
			lock.unlock();
		}

		return results;
	}

	@Override
	public List<V> get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
	{
		if( !lock.tryLock( timeout, unit ) )
			throw new TimeoutException();

		try
		{
			if( results == null )
			{
				long deadline = System.currentTimeMillis() + unit.toMillis( timeout );

				ImmutableList.Builder<V> builder = ImmutableList.builder();
				for( Future<? extends V> future : futures )
				{
					builder.add( future.get( deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS ) );
				}

				results = builder.build();
			}
		}
		finally
		{
			lock.unlock();
		}

		return results;
	}
}
