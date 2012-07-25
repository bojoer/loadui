package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.ControllerApi.find;

import java.util.concurrent.Callable;

import javafx.scene.input.KeyCode;

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
		GUI.getController().click( ".project-ref-view #menuButton" ).press( KeyCode.DOWN ).press( KeyCode.ENTER );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				try
				{
					find( ".project-view" );
					return true;
				}
				catch( NullPointerException e )
				{
					return false;
				}
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
				try
				{
					find( ".workspace-view" );
					return true;
				}
				catch( NullPointerException e )
				{
					return false;
				}
			}
		} );
	}
}
