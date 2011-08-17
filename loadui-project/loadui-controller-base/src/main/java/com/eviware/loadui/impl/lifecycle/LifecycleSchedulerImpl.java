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
package com.eviware.loadui.impl.lifecycle;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.lifecycle.IllegalLifecycleStateException;
import com.eviware.loadui.api.lifecycle.LifecycleScheduler;
import com.eviware.loadui.api.lifecycle.LifecycleTask;
import com.eviware.loadui.api.lifecycle.Phase;
import com.eviware.loadui.api.lifecycle.State;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.Futures;

public class LifecycleSchedulerImpl implements LifecycleScheduler
{
	public static final Logger log = LoggerFactory.getLogger( LifecycleSchedulerImpl.class );

	private final ExecutorService executorService;
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Multimap<Phase, LifecycleTask> tasks = Multimaps.newSetMultimap(
			new HashMap<Phase, Collection<LifecycleTask>>(), new Supplier<Set<LifecycleTask>>()
			{
				@Override
				public Set<LifecycleTask> get()
				{
					return Collections.newSetFromMap( new WeakHashMap<LifecycleTask, Boolean>() );
				}
			} );
	private final StateChanger stateChanger = new StateChanger();

	private State state = State.IDLE;
	private ConcurrentMap<String, Object> currentContext = null;
	private Future<ConcurrentMap<String, Object>> startFuture;
	private Future<ConcurrentMap<String, Object>> stopFuture;

	public LifecycleSchedulerImpl( ExecutorService executor )
	{
		this.executorService = executor;

		registerTask( stateChanger, Phase.START, Phase.STOP );
	}

	@Override
	public State getState()
	{
		return state;
	}

	@Override
	public Future<ConcurrentMap<String, Object>> requestStart( Map<String, Object> initialContext )
			throws IllegalLifecycleStateException
	{
		readWriteLock.writeLock().lock();
		try
		{
			if( currentContext != null )
				throw new IllegalLifecycleStateException( "Life-cycle already in progress!" );

			int concurrencyLevel = 1;
			for( Phase phase : Phase.values() )
			{
				concurrencyLevel = Math.max( concurrencyLevel, tasks.get( phase ).size() );
			}

			currentContext = new MapMaker().concurrencyLevel( concurrencyLevel ).makeMap();
			currentContext.putAll( initialContext );

			return startFuture = executorService.submit( new PhaseRunner( START_PHASES, stopFuture ), currentContext );
		}
		finally
		{
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public Future<ConcurrentMap<String, Object>> requestStop() throws IllegalLifecycleStateException
	{
		readWriteLock.writeLock().lock();
		try
		{
			if( currentContext == null )
				throw new IllegalLifecycleStateException( "No life-cycle in progress!" );

			if( stopFuture == null )
			{
				stopFuture = executorService.submit( new PhaseRunner( STOP_PHASES, startFuture ), currentContext );
				Futures.makeListenable( stopFuture ).addListener( new Runnable()
				{
					@Override
					public void run()
					{
						readWriteLock.writeLock().lock();
						currentContext = null;
						stopFuture = null;
						readWriteLock.writeLock().unlock();
					}
				}, executorService );
			}

			return stopFuture;
		}
		finally
		{
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public synchronized void registerTask( LifecycleTask task, Phase... phases )
	{
		for( Phase phase : phases )
		{
			tasks.put( phase, task );
		}
	}

	@Override
	public synchronized void unregisterTask( LifecycleTask task, Phase... phases )
	{
		for( Phase phase : phases )
		{
			tasks.remove( phase, task );
		}
	}

	private class TaskRunner implements Runnable
	{
		private final LifecycleTask task;
		private final Phase phase;

		public TaskRunner( LifecycleTask task, Phase phase )
		{
			this.task = task;
			this.phase = phase;
		}

		@Override
		public void run()
		{
			readWriteLock.readLock().lock();
			ConcurrentMap<String, Object> context = currentContext;
			readWriteLock.readLock().unlock();
			task.invoke( context, phase );
		}
	}

	private class PhaseRunner implements Runnable
	{
		private final Iterable<Phase> phases;
		private final Future<?> awaitFuture;

		public PhaseRunner( Iterable<Phase> phases, Future<?> awaitFuture )
		{
			this.phases = phases;
			this.awaitFuture = awaitFuture;
		}

		@Override
		public void run()
		{
			if( awaitFuture != null )
			{
				if( !awaitFuture( awaitFuture ) )
				{
					return;
				}
			}

			for( final Phase phase : phases )
			{
				LinkedList<Future<?>> futures = Lists.newLinkedList();
				for( LifecycleTask task : tasks.get( phase ) )
				{
					futures.add( executorService.submit( new TaskRunner( task, phase ) ) );
				}
				for( Future<?> future : futures )
				{
					awaitFuture( future );
				}
			}
		}

		private boolean awaitFuture( Future<?> future )
		{
			try
			{
				future.get();
				return true;
			}
			catch( InterruptedException e )
			{
				log.error( "Error invoking LifecycleTask", e );
			}
			catch( ExecutionException e )
			{
				log.error( "Error invoking LifecycleTask", e );
			}
			return false;
		}
	}

	private class StateChanger implements LifecycleTask
	{
		@Override
		public void invoke( ConcurrentMap<String, Object> context, Phase phase )
		{
			if( Phase.START == phase )
				state = State.RUNNING;
			else if( Phase.STOP == phase )
				state = State.IDLE;
		}
	}
}
