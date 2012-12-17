package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.TestFX.find;
import static com.eviware.loadui.util.test.TestUtils.awaitCondition;

import java.util.concurrent.Callable;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;

public class ProjectCreatedState extends TestState
{
	public static final ProjectCreatedState STATE = new ProjectCreatedState();

	private ProjectCreatedState()
	{
		super( "Project Created", AgentsCreatedState.STATE );
	}
	
	protected ProjectCreatedState( String name, TestState parent ) {
		super( name, parent );
	}

	// This method randomly throws an IndexOutOfBoundsException which breaks the test.
	// TODO: We should look into it once the source code for ObservableList is released.
	@Override
	protected void enterFromParent() throws Exception
	{
		final Node projectCarousel = find( "#projectRefCarousel" );
		log.debug( "Creating new project." );

		GUI.getController().sleep( 2000 ).drag( "#newProjectIcon" ).to( projectCarousel ).type( "Project 1" )
				.type( KeyCode.TAB ).type( "project-1.xml" ).click( ".check-box" ).click( "#default" );

		awaitCondition( new Callable<Boolean>()
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

		awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return projectCarousel.lookup( ".project-ref-view" ) == null;
			}
		} );
	}
}
