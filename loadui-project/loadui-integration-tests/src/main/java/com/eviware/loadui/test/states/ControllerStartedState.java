package com.eviware.loadui.test.states;

import com.eviware.loadui.test.TestState;

public class ControllerStartedState extends TestState
{

	public static final ControllerStartedState STATE = new ControllerStartedState();

	private ControllerStartedState()
	{
		super( "Controller Started", TestState.ROOT );
	}

	@Override
	protected void enterFromParent() throws Exception
	{

	}

	@Override
	protected void exitToParent() throws Exception
	{

	}

}
