package com.eviware.loadui.test.ui.fx;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import javafx.scene.Node;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;

@Category( IntegrationTest.class )
public class ResultViewTest
{

	private static final String PLAY_BUTTON_SELECTOR = ".project-playback-panel #play-button";
	private static TestFX controller;

	@Before
	public void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();
		controller = GUI.getController();
	}

	@After
	public void leaveState() throws Exception
	{
		System.out.println( "Leaving project loaded without agents state" );
		ProjectLoadedWithoutAgentsState.STATE.getParent().enter();
	}

	@Test
	public void executionLanesWorking() throws Exception
	{
		// run a couple of executions
		controller.click( PLAY_BUTTON_SELECTOR ).sleep( 2000 ) // start
		.click( PLAY_BUTTON_SELECTOR ).sleep( 1000 )     // stop
		.click( PLAY_BUTTON_SELECTOR ).sleep( 2000 )     // start
		.click( PLAY_BUTTON_SELECTOR )                   // stop
		.click( "#resultTab" );

		// assert there is a current execution
		getOrFail( "#current-0" );
		assertTrue( TestFX.findAll( "#current-1" ).isEmpty() );

		// assert two results are available
		Node res0 = getOrFail( "#result-node-list #result-0" );
		Node res1 = getOrFail( "#result-node-list #result-1" );
		assertTrue( TestFX.findAll( "#result-2" ).isEmpty() );

		// assert nothing is in archive
		assertTrue( TestFX.findAll( "#archive-0" ).isEmpty() );

		// try to archive an execution
		controller.drag( res1 ).to( "#archive-node-list" );

		// assert there is an archived execution
		getOrFail( "#archive-node-list #archive-0" );
		assertTrue( TestFX.findAll( "#archive-1" ).isEmpty() );

		// assert only one recent execution remains
		getOrFail( "#result-node-list #result-0" );
		assertTrue( TestFX.findAll( "#result-1" ).isEmpty() );

		// try to archive another execution
		controller.drag( res0 ).to( "#archive-node-list" );

		// assert there is two archived executions
		getOrFail( "#archive-node-list #archive-0" );
		getOrFail( "#archive-node-list #archive-1" );
		assertTrue( TestFX.findAll( "#archive-2" ).isEmpty() );

		// assert no recent execution remains
		assertTrue( TestFX.findAll( "#result-0" ).isEmpty() );

		// assert the current execution is still there
		getOrFail( "#current-0" );

	}

	@Test
	public void menuOptions() {
		// run one execution
		controller.click( PLAY_BUTTON_SELECTOR ).sleep( 2000 ) // start
		.click( PLAY_BUTTON_SELECTOR ).sleep( 1000 )     // stop
		.click( "#resultTab" );

		// attempt to use the Open menuItem to open the analysis view
		controller.click( "#current-0 #menuButton" ).click( "#menu-Open" );
		getOrFail( ".analysis-view" );
		
		controller.click( "#close-analysis-view" );

	}

	protected Node getOrFail( String selector )
	{
		try
		{
			return TestFX.find( selector );
		}
		catch( Exception e )
		{
			fail( "Cannot find anything with selector: " + selector );
			return null;
		}
	}

}
