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
package com.eviware.loadui.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Holds a public static final ScheduledThreadPoolExecutor, which cannot be
 * shutdown. This may be reused for scheduling tasks, but be careful not to
 * schedule anything that blocks for too long. The Thread used in this service
 * will not keep the VM alive.
 * 
 * @author dain.nilsson
 */
public final class ScheduledExecutor
{
	public static final ScheduledExecutorService instance = new UnstoppableScheduledExecutorService(
			Executors.newSingleThreadScheduledExecutor( new ThreadFactory()
			{
				@Override
				public Thread newThread( Runnable r )
				{
					Thread thread = new Thread( r, "loadUI ScheduledExecutor" );
					thread.setDaemon( true );
					return thread;
				}
			} ) );

	public static final ScheduledExecutorService getInstance()
	{
		return instance;
	}

	/**
	 * @author dain.nilsson
	 * 
	 */
	private static class UnstoppableScheduledExecutorService implements ScheduledExecutorService
	{
		private final ScheduledExecutorService service;

		public UnstoppableScheduledExecutorService( ScheduledExecutorService service )
		{
			this.service = service;
		}

		@Override
		public void shutdown()
		{
		}

		@Override
		public List<Runnable> shutdownNow()
		{
			return Collections.emptyList();
		}

		@Override
		public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException
		{
			return service.awaitTermination( timeout, unit );
		}

		@Override
		public void execute( Runnable command )
		{
			service.execute( command );
		}

		@Override
		public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
				throws InterruptedException
		{
			return service.invokeAll( tasks, timeout, unit );
		}

		@Override
		public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks ) throws InterruptedException
		{
			return service.invokeAll( tasks );
		}

		@Override
		public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
				throws InterruptedException, ExecutionException, TimeoutException
		{
			return service.invokeAny( tasks, timeout, unit );
		}

		@Override
		public <T> T invokeAny( Collection<? extends Callable<T>> tasks ) throws InterruptedException, ExecutionException
		{
			return service.invokeAny( tasks );
		}

		@Override
		public boolean isShutdown()
		{
			return service.isShutdown();
		}

		@Override
		public boolean isTerminated()
		{
			return service.isTerminated();
		}

		@Override
		public <V> ScheduledFuture<V> schedule( Callable<V> callable, long delay, TimeUnit unit )
		{
			return service.schedule( callable, delay, unit );
		}

		@Override
		public ScheduledFuture<?> schedule( Runnable command, long delay, TimeUnit unit )
		{
			return service.schedule( command, delay, unit );
		}

		@Override
		public ScheduledFuture<?> scheduleAtFixedRate( Runnable command, long initialDelay, long period, TimeUnit unit )
		{
			return service.scheduleAtFixedRate( command, initialDelay, period, unit );
		}

		@Override
		public ScheduledFuture<?> scheduleWithFixedDelay( Runnable command, long initialDelay, long delay, TimeUnit unit )
		{
			return service.scheduleWithFixedDelay( command, initialDelay, delay, unit );
		}

		@Override
		public <T> Future<T> submit( Callable<T> task )
		{
			return service.submit( task );
		}

		@Override
		public <T> Future<T> submit( Runnable task, T result )
		{
			return service.submit( task, result );
		}

		@Override
		public Future<?> submit( Runnable task )
		{
			return service.submit( task );
		}
	}
}
