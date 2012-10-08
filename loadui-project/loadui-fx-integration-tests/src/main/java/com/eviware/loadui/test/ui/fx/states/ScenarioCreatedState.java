package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;

import java.util.concurrent.Callable;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.util.test.TestUtils;

public class ScenarioCreatedState extends TestState
{
	public static final ScenarioCreatedState STATE = new ScenarioCreatedState();
	public static final String SCENARIO_NAME = "Scenario 1";

	private SceneItem scenario = null;

	private ScenarioCreatedState()
	{
		super( "Scenario Created", ProjectLoadedState.STATE );
	}

	public SceneItem getScenario()
	{
		return scenario;
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		log.debug( "Creating scenario." );
		GUI.getController().click( ".project-view #menuButton" ).click( "#newScenario" ).sleep( 100 )
				.click( "#scenario-name" ).type( SCENARIO_NAME ).click( "#default" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll( ".scenario-view" ).isEmpty();
			}
		} );

		ProjectItem project = ProjectLoadedState.STATE.getProject();
		scenario = project.getSceneByLabel( SCENARIO_NAME );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Deleting scenario." );
		GUI.getController().click( ".scenario-view #menuButton" ).click( "#delete" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return findAll( ".scenario-view" ).isEmpty();
			}
		} );
		scenario = null;
	}
}
