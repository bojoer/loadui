/*
 * Copyright 2011 eviware software ab
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Threadpool Executor which keeps a pool of threads, trying not to
 * exceed the max pool size, retiring Threads that have been idle for too long.
 * 
 * @author dain.nilsson
 */
public final class CustomThreadPoolExecutor extends AbstractExecutorService
{
	private static final Logger log = LoggerFactory.getLogger( CustomThreadPoolExecutor.class );

	private static final int IDLE_TIME = 30;
	private static final int MIN_POOL_SIZE = 30;

	private final ThreadFactory threadFactory;
	private final AtomicInteger nWorkers = new AtomicInteger();
	private final AtomicInteger nSleeping = new AtomicInteger();
	private final BlockingQueue<Runnable> workQueue;

	private int maxPoolSize = Integer.MAX_VALUE;

	private boolean isShutdown = false;

	private final static Runnable EXIT_RUNNER = new Runnable()
	{
		public void run()
		{
			throw new RuntimeException();
		}
	};

	public CustomThreadPoolExecutor( int maxPoolSize, BlockingQueue<Runnable> queue, ThreadFactory factory )
	{
		this.maxPoolSize = maxPoolSize;
		workQueue = queue;
		threadFactory = factory;

		for( int i = 0; i < MIN_POOL_SIZE; i++ )
			threadFactory.newThread( new Worker() ).start();

		if( System.getProperty( "loadui.threadpool.status" ) != null )
		{
			Thread statusThread = new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					while( !isShutdown )
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

			statusThread.setDaemon( true );
			statusThread.start();
		}
	}

	public int getUtilization()
	{
		int workers = nWorkers.get();
		return workers == 0 ? 0 : 100 * ( workers - nSleeping.get() ) / getMaxPoolSize();
	}

	@Override
	public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException
	{
		throw new UnsupportedOperationException( "This Executor will not be shutdown!" );
	}

	@Override
	public boolean isShutdown()
	{
		return isShutdown;
	}

	@Override
	public boolean isTerminated()
	{
		return false;
	}

	@Override
	public void shutdown()
	{
		isShutdown = true;
	}

	@Override
	public List<Runnable> shutdownNow()
	{
		shutdown();
		List<Runnable> remaining = new ArrayList<Runnable>();
		workQueue.drainTo( remaining );

		for( int i = nWorkers.get(); i >= 0; i-- )
		{
			if( !workQueue.offer( EXIT_RUNNER ) )
			{
				log.warn( "Failed adding sufficient EXIT_RUNNER commands!" );
				break;
			}
		}

		return remaining;
	}

	@Override
	public void execute( Runnable command )
	{
		if( isShutdown )
			return;

		// This may fail. If so, we ignore it...
		if( !workQueue.offer( command ) )
			;

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
				Runnable r = null;
				try
				{
					nSleeping.incrementAndGet();
					r = workQueue.poll( isShutdown ? 0 : IDLE_TIME, TimeUnit.SECONDS );
					nSleeping.decrementAndGet();
					if( r == null )
					{
						if( nWorkers.get() > MIN_POOL_SIZE || ( isShutdown && workQueue.isEmpty() ) )
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
				catch( RuntimeException e )
				{
					if( r == EXIT_RUNNER )
					{
						exit = true;
					}
					else
						e.printStackTrace();
				}
			}
			nWorkers.decrementAndGet();
		}
	}
}