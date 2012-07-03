package com.eviware.loadui.test.ui.fx.states;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;

public class FXAppLoadedState extends TestState
{
	public static final TestState STATE = new FXAppLoadedState();

	private FXAppLoadedState()
	{
		super( "FX App Loaded", TestState.ROOT );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		GUI.getBundleContext();
	}

	@Override
	protected void exitToParent() throws Exception
	{
		// TODO Auto-generated method stub

	}
}
