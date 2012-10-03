package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;

import java.util.concurrent.Callable;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.ui.fx.util.test.TestFX;
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
		log.debug( "Opening project." );
		GUI.getController().click( ".project-ref-view #menuButton" ).click( "#open" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll( ".project-view" ).isEmpty();
			}
		} );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Closing project." );
		GUI.getController().click( "#closeProjectButton" );
		//If there is a save dialog, do not save:
		try
		{
			GUI.getController().click( "#no" ).target( TestFX.getWindowByIndex( 0 ) );
		}
		catch( Exception e )
		{
			//Ignore
		}

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll( ".workspace-view" ).isEmpty();
			}
		} );
	}
}
