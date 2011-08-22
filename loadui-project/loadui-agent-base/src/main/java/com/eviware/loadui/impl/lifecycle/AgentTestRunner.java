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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.eviware.loadui.api.lifecycle.TestExecution;
import com.eviware.loadui.api.lifecycle.Phase;
import com.eviware.loadui.api.lifecycle.TestExecutionTask;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.execution.AbstractTestRunner;
import com.google.common.collect.Maps;

/**
 * Listens for remote invocations of lifecycle phases. When a phase is started
 * remotely, the AgentLifecycleScheduler will be notified and will run its
 * registered tasks, before sending a reply notifying the remote controller that
 * the agent is done.
 * 
 * @author dain.nilsson
 */
public class AgentTestRunner extends AbstractTestRunner implements Releasable
{
	private static final String CHANNEL = "/lifecycleAddon";

	private final PhaseMessageListener phaseListener = new PhaseMessageListener();
	private final HashMap<String, AgentTestExecution> executions = Maps.newHashMap();
	private final MessageEndpoint endpoint;
	private final ExecutorService executorService;
	private final TestExecutionTask task;

	public AgentTestRunner( MessageEndpoint endpoint, ExecutorService executorService )
	{
		super( executorService );
		this.endpoint = endpoint;
		this.executorService = executorService;

		endpoint.addMessageListener( CHANNEL, phaseListener );

		registerTask( task = new TestExecutionTask()
		{
			@Override
			public void invoke( TestExecution execution, Phase phase )
			{
				System.out.println( "Phase: " + phase + " execution: " + execution );
				try
				{
					Thread.sleep( 1000 );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		}, Phase.values() );
	}

	@Override
	public TestExecution enqueueExecution( CanvasItem canvas )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<TestExecution> getExecutionQueue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void release()
	{
		endpoint.removeMessageListener( phaseListener );
	}

	private class PhaseMessageListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, final MessageEndpoint endpoint, final Object data )
		{
			if( CHANNEL.equals( channel ) )
			{
				Object[] strings = ( Object[] )data;
				final String canvasId = ( String )strings[0];
				final Phase phase = Phase.valueOf( ( String )strings[1] );

				if( phase == Phase.PRE_START )
				{
					executions.put( canvasId, new AgentTestExecution( canvasId ) );
				}

				final AgentTestExecution execution = executions.get( canvasId );

				if( phase == Phase.POST_STOP )
				{
					executions.remove( canvasId );
				}

				executorService.execute( new Runnable()
				{
					@Override
					public void run()
					{
						awaitFuture( runPhase( phase, execution ) );
						endpoint.sendMessage( CHANNEL, data );
					}
				} );
			}
		}
	}
}
