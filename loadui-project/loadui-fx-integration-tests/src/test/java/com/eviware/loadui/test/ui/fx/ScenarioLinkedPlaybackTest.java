package com.eviware.loadui.test.ui.fx;

import static org.junit.Assert.assertTrue;
import javafx.scene.input.KeyCode;

import org.junit.AfterClass;
import org.junit.BeforeClass;
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
	private final SceneItem scenario = ScenarioCreatedState.STATE.getScenario();

	@BeforeClass
	public static void enterState() throws Exception
	{
		ScenarioCreatedState.STATE.enter();
		controller = GUI.getController();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ScenarioCreatedState.STATE.getParent().enter();
	}

	@Test
	public void shouldFollowProject_when_linked() throws Exception
	{
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
				.sleep( 1000 ).click( ".project-playback-panel #play-button" ).sleep( 2000 );
		assertTrue( project.isRunning() );

		controller.sleep( 4000 );
		assertTrue( !project.isRunning() );
	}

	protected void clickPlayStopButton()
	{
		controller.click( ".project-playback-panel #play-button" ).sleep( 2000 );
	}
}
