package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;

import java.util.concurrent.Callable;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.util.test.TestUtils;

public class TwoScenariosCreatedState extends TestState
{
	public static final TwoScenariosCreatedState STATE = new TwoScenariosCreatedState();
	public static final String[] SCENARIO_NAME = { "Scenario 1", "Scenario 2" };

	private TwoScenariosCreatedState()
	{
		super( "Scenario Created", ProjectLoadedWithoutAgentsState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		log.debug( "Creating scenarios." );
		GUI.getController().drag( "#newScenarioIcon" ).by( 300, 0 ).drop().sleep( 100 ).click( "#scenario-name" )
				.type( SCENARIO_NAME[0] ).click( "#default" );
		GUI.getController().drag( "#newScenarioIcon" ).by( 400, 100 ).drop().sleep( 100 ).click( "#scenario-name" )
				.type( SCENARIO_NAME[1] ).click( "#default" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return findAll( ".scenario-view" ).size() == 2;
			}
		} );

	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Deleting scenarios." );

		GUI.getController().click( ".scenario-view #menu" ).click( "#delete" );
		GUI.getController().click( ".scenario-view #menu" ).click( "#delete" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return findAll( ".scenario-view" ).isEmpty();
			}
		} );
	}
}
