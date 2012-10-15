package com.eviware.loadui.test.ui.fx;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.test.categories.IntegrationTest;
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

	protected void clickPlayStopButton()
	{
		controller.click( ".project-playback-panel #play-button" ).sleep( 4000 );
	}
}
