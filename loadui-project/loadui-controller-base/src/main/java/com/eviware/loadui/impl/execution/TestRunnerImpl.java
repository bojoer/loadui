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
package com.eviware.loadui.impl.execution;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.eviware.loadui.api.execution.ExecutionResult;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestState;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.execution.AbstractTestRunner;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;

public class TestRunnerImpl extends AbstractTestRunner implements Releasable
{
	private static final Function<TestController, TestExecution> getExecution = new Function<TestController, TestExecution>()
	{
		@Override
		public TestExecution apply( TestController input )
		{
			return input.execution;
		}
	};

	private final ExecutorService testExecutor = MoreExecutors.listeningDecorator( Executors.newSingleThreadExecutor() );
	private final LinkedList<TestController> executionQueue = Lists.newLinkedList();

	public TestRunnerImpl( ExecutorService executor )
	{
		super( executor );
	}

	@Override
	public TestExecution enqueueExecution( CanvasItem canvas )
	{
		TestController controller = new TestController( new TestExecutionImpl( canvas ) );

		synchronized( executionQueue )
		{
			executionQueue.add( controller );
		}

		return controller.execution;
	}

	@Override
	public List<TestExecution> getExecutionQueue()
	{
		synchronized( executionQueue )
		{
			return ImmutableList.copyOf( Iterables.transform( executionQueue, getExecution ) );
		}
	}

	@Override
	public void release()
	{
		testExecutor.shutdown();
	}

	class TestController
	{
		private final TestExecutionImpl execution;
		private final Future<ExecutionResult> resultFuture;
		private boolean stopping = false;

		private TestController( TestExecutionImpl execution )
		{
			this.execution = execution;
			execution.setController( this );
			resultFuture = testExecutor.submit( new TestRunnable( execution, this ) );
		}

		public Future<ExecutionResult> getExecutionFuture()
		{
			return resultFuture;
		}

		public void initStop()
		{
			synchronized( this )
			{
				stopping = true;
				if( execution.isAborted() && execution.getState() == TestState.ENQUEUED )
				{
					if( resultFuture.cancel( false ) )
					{
						synchronized( executionQueue )
						{
							executionQueue.remove( this );
						}
					}
				}
				notifyAll();
			}
		}

		private void awaitStopping()
		{
			synchronized( this )
			{
				while( !stopping )
				{
					try
					{
						wait();
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	private class TestRunnable implements Callable<ExecutionResult>
	{
		private final TestExecutionImpl execution;
		private final TestController controller;

		public TestRunnable( TestExecutionImpl execution, TestController controller )
		{
			this.execution = execution;
			this.controller = controller;
		}

		@Override
		public ExecutionResult call()
		{
			try
			{
				if( !execution.isAborted() )
				{
					log.debug( "Starting TestExecution: {}", execution );
					execution.setState( TestState.STARTING );
					awaitFuture( runPhase( Phase.PRE_START, execution ) );
					execution.setState( TestState.RUNNING );
					awaitFuture( runPhase( Phase.START, execution ) );
					awaitFuture( runPhase( Phase.POST_START, execution ) );

					controller.awaitStopping();

					execution.setState( TestState.STOPPING );
					awaitFuture( runPhase( Phase.PRE_STOP, execution ) );
					awaitFuture( runPhase( Phase.STOP, execution ) );
					awaitFuture( runPhase( Phase.POST_STOP, execution ) );
					execution.setState( TestState.COMPLETED );
					log.debug( "Completed TestExecution: {}", execution );
				}
				else
				{
					log.warn( "Will not start as Execution was aborted" );
				}

				//TODO: Create ExecutionResult
				return null;
			}
			finally
			{
				synchronized( executionQueue )
				{
					executionQueue.remove( controller );
				}
			}
		}
	}
}
