package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.TestFX.find;

import java.util.concurrent.Callable;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.util.test.TestUtils;

public class AgentsCreatedState extends TestState
{
	public static final AgentsCreatedState STATE = new AgentsCreatedState();

	private AgentsCreatedState()
	{
		super( "3 Agents Created", FXAppLoadedState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		final Node agentCarousel = find( "#agentCarousel" );

		log.debug( "Creating new Agent by draggning." );
		GUI.getController().drag( "#newAgentIcon" ).to( agentCarousel ).type( "Fake agent1" ).type( KeyCode.TAB )
				.type( "notAURL1" ).click( "#default" );

		log.debug( "Creating new Agent by doubleclicking." );
		GUI.getController().click( "#newAgentIcon" ).click( "#newAgentIcon" ).type( "Fake agent2" ).type( KeyCode.TAB )
				.type( "notAURL2" ).click( "#default" );

		log.debug( "Creating new Agent by rightclicking on carousel" );
		GUI.getController().click( agentCarousel, MouseButton.SECONDARY ).sleep( 100l ).click( "#add-agent-menu-button" )
				.type( "Fake agent3" ).type( KeyCode.TAB ).type( "notAURL3" ).click( "#default" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return agentCarousel.lookup( ".agent-view" ) != null;
			}
		} );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Deleting project agents." );
		GUI.getController().click( "#agentCarousel .agent-view .menu-button" ).click( "#delete" );
		GUI.getController().click( "#agentCarousel .agent-view .menu-button" ).click( "#delete" );
		GUI.getController().click( "#agentCarousel .agent-view .menu-button" ).click( "#delete" );
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
