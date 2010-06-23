/*
 * Copyright 2010 eviware software ab
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

package com.eviware.loadui.util.dispatch;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom Threadpool Executor which keeps a pool of threads, trying not to
 * exceed the max pool size, retiring Threads that have been idle for too long.
 * 
 * @author dain.nilsson
 */
public class CustomThreadPoolExecutor extends AbstractExecutorService
{
	private static final int IDLE_TIME = 30;
	private static final int MIN_POOL_SIZE = 30;

	private final BlockingQueue<Runnable> workQueue;
	private final ThreadFactory threadFactory;
	private final AtomicInteger nWorkers = new AtomicInteger();
	private final AtomicInteger nSleeping = new AtomicInteger();

	private int maxPoolSize = Integer.MAX_VALUE;

	public CustomThreadPoolExecutor( int maxPoolSize, BlockingQueue<Runnable> queue, ThreadFactory factory )
	{
		this.maxPoolSize = maxPoolSize;
		workQueue = queue;
		threadFactory = factory;

		for( int i = 0; i < MIN_POOL_SIZE; i++ )
			threadFactory.newThread( new Worker() ).start();

		Thread thread = new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				while( true )
				{
					try
					{
						Thread.sleep( 5000 );
					}
					catch( InterruptedException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int workers = nWorkers.get();
					int sleeping = nSleeping.get();
					System.out.println( "Active: " + ( workers - sleeping ) + " Workers: " + workers + " Sleeping: "
							+ sleeping + " Queue size: " + workQueue.size() + " Max pool size: " + getMaxPoolSize() );
				}
			}
		} );

		thread.setDaemon( true );
		thread.start();
	}

	@Override
	public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException
	{
		throw new UnsupportedOperationException( "This Executor will not be shutdown!" );
	}

	@Override
	public boolean isShutdown()
	{
		return false;
	}

	@Override
	public boolean isTerminated()
	{
		return false;
	}

	@Override
	public void shutdown()
	{
		throw new UnsupportedOperationException( "This Executor may not be shutdown!" );
	}

	@Override
	public List<Runnable> shutdownNow()
	{
		throw new UnsupportedOperationException( "This Executor may not be shutdown!" );
	}

	@Override
	public void execute( Runnable command )
	{
		// This may fail. If so, we ignore it...
		workQueue.offer( command );

		// If we have no idle Workers, and are allowed to create a new one, do so.
		if( nSleeping.get() == 0 && nWorkers.get() < maxPoolSize )
			threadFactory.newThread( new Worker() ).start();
	}

	public int getMaxPoolSize()
	{
		return maxPoolSize;
	}

	public void setMaxPoolSize( int maxPoolSize )
	{
		this.maxPoolSize = maxPoolSize;
	}

	private class Worker implements Runnable
	{
		private boolean exit = false;

		@Override
		public void run()
		{
			nWorkers.incrementAndGet();
			while( !exit )
			{
				try
				{
					nSleeping.incrementAndGet();
					Runnable r = workQueue.poll( IDLE_TIME, TimeUnit.SECONDS );
					nSleeping.decrementAndGet();
					if( r == null )
					{
						if( nWorkers.get() > MIN_POOL_SIZE )
							exit = true;
					}
					else
					{
						r.run();
					}
				}
				catch( InterruptedException e )
				{
					// Ignore
				}
			}
			nWorkers.decrementAndGet();
		}
	}
}