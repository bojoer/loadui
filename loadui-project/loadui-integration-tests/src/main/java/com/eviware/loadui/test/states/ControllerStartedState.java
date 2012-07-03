package com.eviware.loadui.test.states;

import org.osgi.framework.BundleContext;

import com.eviware.loadui.test.ControllerWrapper;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.util.BeanInjector;

public class ControllerStartedState extends TestState
{
	public static final ControllerStartedState STATE = new ControllerStartedState();

	private ControllerWrapper controller;

	private ControllerStartedState()
	{
		super( "Controller Started", TestState.ROOT );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		controller = new ControllerWrapper();
		BeanInjector.setBundleContext( controller.getBundleContext() );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		controller.stop();
	}

	public BundleContext getBundleContext()
	{
		return controller.getBundleContext();
	}
}
