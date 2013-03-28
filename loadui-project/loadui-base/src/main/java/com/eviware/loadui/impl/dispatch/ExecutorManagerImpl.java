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
package com.eviware.loadui.impl.dispatch;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.dispatch.ExecutorManager;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.util.collections.ResizableBlockingQueue;
import com.eviware.loadui.util.dispatch.CustomThreadPoolExecutor;

public final class ExecutorManagerImpl implements ExecutorManager
{
	private final static int MIN_POOL_SIZE = 10;
	private final static int DEFAULT_POOL_SIZE = 1000;
	private final static int DEFAULT_QUEUE_SIZE = 10000;

	public final static Logger log = LoggerFactory.getLogger( ExecutorManagerImpl.class );

	private final CustomThreadPoolExecutor executor;
	private final ResizableBlockingQueue<Runnable> queue = new ResizableBlockingQueue<>( DEFAULT_QUEUE_SIZE );

	public ExecutorManagerImpl( final WorkspaceProvider workspaceProvider )
	{
		executor = new CustomThreadPoolExecutor( DEFAULT_POOL_SIZE, queue, new ThreadFactory()
		{
			private final AtomicInteger threadCount = new AtomicInteger( 1 );

			@Override
			public Thread newThread( Runnable r )
			{
				final Thread thread = new Thread( r, "executor-thread-" + threadCount.getAndIncrement() );
				thread.setDaemon( true );
				try
				{
					if( EventQueue.isDispatchThread() )
						thread.setContextClassLoader( Thread.currentThread().getContextClassLoader() );
					else
					{
						EventQueue.invokeAndWait( new Runnable()
						{
							@Override
							public void run()
							{
								thread.setContextClassLoader( Thread.currentThread().getContextClassLoader() );
							}
						} );
					}
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
				catch( InvocationTargetException e )
				{
					e.printStackTrace();
				}
				return thread;
			}
		} );

		Thread thread = new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				if( LoadUI.isController() )
				{
					while( !workspaceProvider.isWorkspaceLoaded() )
					{
						try
						{
							Thread.sleep( 500 );
						}
						catch( InterruptedException e )
						{
							// Ignore
						}
					}

					WorkspaceItem workspace = workspaceProvider.getWorkspace();
					workspace.addEventListener( PropertyEvent.class, new EventHandler<PropertyEvent>()
					{
						@Override
						public void handleEvent( PropertyEvent event )
						{
							if( WorkspaceItem.MAX_THREADS_PROPERTY.equals( event.getKey() ) )
								setMaxPoolSize( ( ( Number )event.getProperty().getValue() ).intValue() );
							if( WorkspaceItem.MAX_THREAD_QUEUE_PROPERTY.equals( event.getKey() ) )
								setMaxQueueSize( ( ( Number )event.getProperty().getValue() ).intValue() );
						}
					} );
					setMaxPoolSize( ( ( Number )workspace.getProperty( WorkspaceItem.MAX_THREADS_PROPERTY ).getValue() ).intValue() );
					setMaxQueueSize( ( ( Number )workspace.getProperty( WorkspaceItem.MAX_THREAD_QUEUE_PROPERTY ).getValue() )
							.intValue() );
				}
			}
		}, "Awaiting Initialization" );

		thread.setDaemon( true );
		thread.start();
	}

	@Override
	public ExecutorService getExecutor()
	{
		return executor;
	}

	@Override
	public int getMaxPoolSize()
	{
		return executor.getMaxPoolSize();
	}

	@Override
	public void setMaxPoolSize( int size )
	{
		int newSize = size < MIN_POOL_SIZE ? MIN_POOL_SIZE : size;
		executor.setMaxPoolSize( newSize );
		log.debug( "Global Threadpool max size set to {}", newSize );
	}

	@Override
	public int getMaxQueueSize()
	{
		return queue.getCapacity();
	}

	@Override
	public void setMaxQueueSize( int size )
	{
		log.debug( "Global Threadpool queue max size set to {}", size );
		queue.setCapacity( size );
	}

	public void shutdown()
	{
		executor.shutdownNow();
	}
}
