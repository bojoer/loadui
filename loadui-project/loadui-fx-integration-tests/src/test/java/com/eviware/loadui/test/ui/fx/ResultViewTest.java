package com.eviware.loadui.test.ui.fx;

import static com.eviware.loadui.ui.fx.util.test.FXTestUtils.failIfExists;
import static com.eviware.loadui.ui.fx.util.test.FXTestUtils.getOrFail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.input.KeyCode;

import org.junit.After;
import org.junit.Before;
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
		Set<Node> resultViewSet = TestFX.findAll( ".result-view" );
		if( !resultViewSet.isEmpty() )
		{
			GUI.getController().closeCurrentWindow();
		}
		ProjectLoadedWithoutAgentsState.STATE.getParent().enter();
	}

	@Test
	public void executionLanesWorking() throws Exception
	{
		// run a couple of executions
		controller.click( PLAY_BUTTON_SELECTOR ).sleep( 2000 ) // start
				.click( PLAY_BUTTON_SELECTOR ).sleep( 1000 ) // stop
				.click( PLAY_BUTTON_SELECTOR ).sleep( 2000 ) // start
				.click( PLAY_BUTTON_SELECTOR ) // stop
				.click( "#statsTab" ).sleep( 500 ).click( "#open-execution" ).sleep( 500 );

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

	}

	@Test
	public void menuOptionsAreCorrectAndWorking()
	{
		// run one execution
		controller.click( PLAY_BUTTON_SELECTOR ).sleep( 2000 ) // start
				.click( PLAY_BUTTON_SELECTOR ).sleep( 1000 ) // stop
				.click( "#statsTab" ).sleep( 500 ).click( "#open-execution" ).sleep( 500 );

		// check recent execution menu's options
		controller.click( "#result-0 #menuButton" );
		getOrFail( "#menu-Open" );
		getOrFail( "#menu-Delete" );
		failIfExists( "#menu-Rename" );

		// check if Open option works
		controller.click( "#menu-Open" );
		getOrFail( ".analysis-view" );
		getOrFail( "#open-execution" );

		controller.click( "#open-execution" );

		// check archive execution menu's options
		controller.drag( "#result-0" ).to( "#archive-node-list" ).click( "#archive-0 #menuButton" );
		getOrFail( "#menu-Open" );
		getOrFail( "#menu-Delete" );
		getOrFail( "#menu-Rename" );

		// test rename function
		controller.click( "#menu-Rename" ).type( "Renamed Execution" ).type( KeyCode.ENTER );
		MenuButton menuButton = ( MenuButton )getOrFail( "#archive-0 #menuButton" );
		assertEquals( "Renamed Execution", menuButton.textProperty().get() );

		// delete execution
		controller.click( "#archive-0 #menuButton" ).click( "#menu-Delete" );
		failIfExists( "#archive-0" );

	}

}
