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
package com.eviware.loadui.test.ui.fx;

import static org.junit.Assert.assertTrue;
import javafx.scene.input.KeyCode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.test.ui.fx.states.ScenarioCreatedState;
import com.eviware.loadui.ui.fx.util.test.TestFX;

@Category( IntegrationTest.class )
public class ScenarioLinkedPlaybackTest
{
	private static TestFX controller;

	@Before
	public void enterState() throws Exception
	{
		ScenarioCreatedState.STATE.enter();
		controller = GUI.getController();
	}

	@After
	public void leaveState() throws Exception
	{
		ScenarioCreatedState.STATE.getParent().enter();
	}

	@Test
	public void shouldFollowProject_when_linked() throws Exception
	{
		SceneItem scenario = ScenarioCreatedState.STATE.getScenario();
		assertTrue( scenario.isFollowProject() );

		for( int i = 0; i < 5; i++ )
		{
			clickPlayStopButton();
			assertTrue( scenario.isRunning() );

			clickPlayStopButton();
			assertTrue( !scenario.isRunning() );
		}
	}

	@Test
	public void shouldNotFollowProject_when_unLinked() throws Exception
	{
		SceneItem scenario = ScenarioCreatedState.STATE.getScenario();
		assertTrue( scenario.isFollowProject() );

		controller.click( "#link-scenario" ).sleep( 500 );

		for( int i = 0; i < 3; i++ )
		{
			clickPlayStopButton();
			assertTrue( !scenario.isRunning() );

			clickPlayStopButton();
			assertTrue( !scenario.isRunning() );
		}
	}

	@Test
	public void shouldStopOnLimit_when_isLinked() throws Exception
	{
		ProjectItem project = ProjectLoadedWithoutAgentsState.STATE.getProject();

		controller.click( "#set-limits" ).click( "#time-limit" ).press( KeyCode.CONTROL, KeyCode.A )
				.release( KeyCode.CONTROL, KeyCode.A ).sleep( 100 ).type( "6" ).sleep( 100 ).click( "#default" )
				.sleep( 1000 ).click( ".project-playback-panel .play-button" ).sleep( 2000 );
		assertTrue( project.isRunning() );

		controller.sleep( 4000 );
		assertTrue( !project.isRunning() );
	}

	protected void clickPlayStopButton()
	{
		controller.click( ".project-playback-panel .play-button" ).sleep( 2000 );
	}
}
