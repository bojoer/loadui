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
package com.eviware.loadui.impl.execution;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import static org.mockito.Mockito.*;

import com.eviware.loadui.api.execution.ExecutionResult;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.impl.execution.TestRunnerImpl;

public class TestRunnerImplTest
{
	private TestRunnerImpl testRunner;
	private CanvasItem canvasMock;

	@Before
	public void setup()
	{
		testRunner = new TestRunnerImpl( Executors.newCachedThreadPool() );
		canvasMock = mock( CanvasItem.class );
	}

	@Test
	public void shouldCompleteTestExecution() throws Exception
	{
		TestExecution execution = testRunner.enqueueExecution( canvasMock );
		Future<ExecutionResult> future = execution.complete();

		future.get( 1, TimeUnit.SECONDS );
	}

	@Test
	public void shouldHandleMultipleStopRequests() throws Exception
	{
		TestExecution execution = testRunner.enqueueExecution( canvasMock );

		execution.complete();

		Future<ExecutionResult> future1 = execution.complete();
		Future<ExecutionResult> future2 = execution.complete();
		Future<ExecutionResult> future3 = execution.complete();

		future3.get( 1, TimeUnit.SECONDS );
		future1.get( 1, TimeUnit.SECONDS );
		future2.get( 1, TimeUnit.SECONDS );
	}
}
