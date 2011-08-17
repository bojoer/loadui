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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.lifecycle.IllegalLifecycleStateException;
import com.eviware.loadui.api.lifecycle.LifecycleScheduler;
import com.eviware.loadui.api.lifecycle.LifecycleTask;
import com.eviware.loadui.api.lifecycle.Phase;
import com.eviware.loadui.api.lifecycle.State;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.traits.Releasable;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Listens for remote invocations of lifecycle phases. When a phase is started
 * remotely, the AgentLifecycleScheduler will be notified and will run its
 * registered tasks, before sending a reply notifying the remote controller that
 * the agent is done.
 * 
 * @author dain.nilsson
 */
public class AgentLifecycleScheduler implements LifecycleScheduler, Releasable
{
	private static final Logger log = LoggerFactory.getLogger( AgentLifecycleScheduler.class );
	private static final String CHANNEL = "/lifecycleAddon";

	private final Multimap<Phase, LifecycleTask> tasks = Multimaps.newSetMultimap(
			new HashMap<Phase, Collection<LifecycleTask>>(), new Supplier<Set<LifecycleTask>>()
			{
				@Override
				public Set<LifecycleTask> get()
				{
					return Collections.newSetFromMap( new WeakHashMap<LifecycleTask, Boolean>() );
				}
			} );
	private final PhaseMessageListener phaseListener = new PhaseMessageListener();
	private final StateChanger stateChanger = new StateChanger();
	private final MessageEndpoint endpoint;
	private final ExecutorService executorService;
	private State state = State.IDLE;

	public AgentLifecycleScheduler( MessageEndpoint endpoint, ExecutorService executorService )
	{
		this.endpoint = endpoint;
		this.executorService = executorService;

		endpoint.addMessageListener( CHANNEL, phaseListener );
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
		throw new UnsupportedOperationException();
	}

	@Override
	public Future<ConcurrentMap<String, Object>> requestStop() throws IllegalLifecycleStateException
	{
		throw new UnsupportedOperationException();
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

	@Override
	public void release()
	{
		endpoint.removeMessageListener( phaseListener );
		unregisterTask( stateChanger, Phase.START, Phase.STOP );
	}

	private static class TaskRunner implements Runnable
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
			task.invoke( Maps.<String, Object> newConcurrentMap(), phase );
		}
	}

	private class PhaseMessageListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			if( CHANNEL.equals( channel ) )
			{
				Phase phase = Phase.valueOf( ( String )data );
				LinkedList<Future<?>> futures = Lists.newLinkedList();
				for( LifecycleTask task : tasks.get( phase ) )
				{
					futures.add( executorService.submit( new TaskRunner( task, phase ) ) );
				}
				for( Future<?> future : futures )
				{
					awaitFuture( future );
				}
				endpoint.sendMessage( CHANNEL, phase );
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
