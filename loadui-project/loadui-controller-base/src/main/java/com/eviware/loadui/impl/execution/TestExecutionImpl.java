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

import java.util.concurrent.Future;

import com.eviware.loadui.api.execution.ExecutionResult;
import com.eviware.loadui.api.execution.TestState;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.testevents.MessageLevel;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.impl.execution.TestRunnerImpl.TestController;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.execution.AbstractTestExecution;

public class TestExecutionImpl extends AbstractTestExecution
{
	private TestController controller;

	public TestExecutionImpl( CanvasItem canvas )
	{
		super( canvas );
	}

	@Override
	public Future<ExecutionResult> complete()
	{
		controller.initStop();
		return controller.getExecutionFuture();
	}

	@Override
	public Future<ExecutionResult> abort( String reason )
	{
		//TestExecutionEvent.logExecutionEvent( TestExecutionEvent.ExecutionAction.ABORTED );
		BeanInjector.getBean( TestEventManager.class ).logMessage( MessageLevel.WARNING, "Test aborted: " + reason );
		return super.abort( reason );
	}

	void setController( TestController controller )
	{
		this.controller = controller;
	}

	void setState( TestState state )
	{
		this.state = state;
	}
}
