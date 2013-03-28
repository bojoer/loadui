/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
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
		super( "Scenario Created", ProjectLoadedWithoutAgentsState.STATE );
	}

	public SceneItem getScenario()
	{
		return scenario;
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		log.debug( "Creating scenario." );
		GUI.getController().drag( "#newScenarioIcon" ).by( 300, 0 ).drop().sleep( 100 ).click( "#scenario-name" )
				.type( SCENARIO_NAME ).click( "#default" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll( ".scenario-view" ).isEmpty();
			}
		} );

		ProjectItem project = ProjectLoadedWithoutAgentsState.STATE.getProject();
		scenario = project.getSceneByLabel( SCENARIO_NAME );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Deleting scenario." );

		GUI.getController().click( ".scenario-view #menu" ).click( "#delete-item" ).click( ".confirmation-dialog #default" );

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
