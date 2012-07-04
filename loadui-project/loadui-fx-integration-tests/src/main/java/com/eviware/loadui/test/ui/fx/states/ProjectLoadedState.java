package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.ControllerApi.find;
import static com.eviware.loadui.ui.fx.util.test.ControllerApi.offset;

import java.util.concurrent.Callable;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.util.test.TestUtils;

public class ProjectLoadedState extends TestState
{
	public static final TestState STATE = new ProjectLoadedState();

	private ProjectLoadedState()
	{
		super( "Project Loaded", ProjectCreatedState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		GUI.getController().click( offset( ".project-ref-node", 45, 75 ) );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return find( ".project-view" ) != null;
			}
		} );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		GUI.getController().click( "#closeProjectButton" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return find( ".workspace-view" ) != null;
			}
		} );
	}
}
