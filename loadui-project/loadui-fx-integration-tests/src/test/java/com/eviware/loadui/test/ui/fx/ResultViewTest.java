package com.eviware.loadui.test.ui.fx;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.LastResultOpenedState;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;

@Category( IntegrationTest.class )
public class ResultViewTest
{

	private static final String PLAY_BUTTON_SELECTOR = ".project-playback-panel #play-button";
	private static TestFX controller;

	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();
		controller = GUI.getController();
		controller.click( PLAY_BUTTON_SELECTOR ).sleep( 2000 )
			.click( PLAY_BUTTON_SELECTOR ).click( "#resultTab" );
		System.out.println( "Opened results tab" );
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		System.out.println( "Leaving project loaded without agents state" );
		ProjectLoadedWithoutAgentsState.STATE.getParent().enter();
	}

	@Test
	public void basics() throws Exception
	{
		System.out.println( "Doing basics test" );
		
		
//		StatisticPages pages = ProjectLoadedWithoutAgentsState.STATE.getProject().getStatisticPages();
//		assertEquals( 1, pages.getChildCount() );
//
//		controller.click( "#plus-button" ).click( "#untitled-page-2" ).click( "#untitled-page-1" ).click( "#plus-button" );
//		assertEquals( 3, pages.getChildCount() );
//
//		controller.click( "#untitled-page-2" ).click( "#untitled-page-2 .tab-close-button" ).click( "#plus-button" )
//				.click( "#untitled-page-1" ).click( "#untitled-page-1 .tab-close-button" );
//		assertEquals( 2, pages.getChildCount() );
//		assertEquals( "Untitled Page 3", pages.getChildAt( 0 ).getLabel() );
//		assertEquals( "Untitled Page 4", pages.getChildAt( 1 ).getLabel() );
	}
}
