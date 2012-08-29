package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.ControllerApi.find;

import java.util.concurrent.Callable;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.util.test.TestUtils;

public class ProjectCreatedState extends TestState
{
	public static final TestState STATE = new ProjectCreatedState();

	private ProjectCreatedState()
	{
		super( "Project Created", FXAppLoadedState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		final Node projectCarousel = find( "#projectRefCarousel" );
		log.debug( "Creating new project." );
		GUI.getController().drag( "#newProjectIcon" ).to( projectCarousel ).type( "Project 1" ).type( KeyCode.TAB )
				.type( "project-1.xml" ).click( ".check-box" ).click( "#default" );

		//GUI.getController().click( projectCarousel, MouseButton.SECONDARY ).moveBy( 15, 10 ).click();

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return projectCarousel.lookup( ".project-ref-view" ) != null;
			}
		} );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Deleting project." );
		GUI.getController().click( "#projectRefCarousel .project-ref-view .menu-button" ).click( "#delete" );
		final Node projectCarousel = find( "#projectRefCarousel" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return projectCarousel.lookup( ".project-ref-view" ) == null;
			}
		} );
	}
}
