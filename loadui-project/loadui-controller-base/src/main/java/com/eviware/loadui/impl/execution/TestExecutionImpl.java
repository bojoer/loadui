package com.eviware.loadui.impl.execution;

import java.util.concurrent.Future;

import com.eviware.loadui.api.execution.ExecutionResult;
import com.eviware.loadui.api.execution.TestState;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.impl.execution.TestRunnerImpl.TestController;
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
	public Future<ExecutionResult> abort()
	{
		TestExecutionEvent.logExecutionEvent( TestExecutionEvent.ExecutionAction.ABORTED );
		return super.abort();
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
