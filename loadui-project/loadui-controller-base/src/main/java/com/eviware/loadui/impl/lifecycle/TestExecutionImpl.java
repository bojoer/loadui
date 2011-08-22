package com.eviware.loadui.impl.lifecycle;

import java.util.concurrent.Future;

import com.eviware.loadui.api.lifecycle.ExecutionResult;
import com.eviware.loadui.api.lifecycle.TestState;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.impl.lifecycle.TestRunnerImpl.TestController;
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

	void setController( TestController controller )
	{
		this.controller = controller;
	}

	void setState( TestState state )
	{
		this.state = state;
	}
}
