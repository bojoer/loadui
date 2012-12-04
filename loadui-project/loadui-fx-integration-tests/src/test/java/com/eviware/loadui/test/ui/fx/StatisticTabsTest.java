package com.eviware.loadui.test.ui.fx;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.LastResultOpenedState;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedState;
import com.eviware.loadui.ui.fx.util.test.TestFX;

@Category( IntegrationTest.class )
public class StatisticTabsTest
{
	private static TestFX controller;

	@BeforeClass
	public static void enterState() throws Exception
	{
		LastResultOpenedState.STATE.enter();
		controller = GUI.getController();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ProjectLoadedState.STATE.getParent().enter();
	}

	@Test
	public void basics() throws Exception
	{
		StatisticPages pages = ProjectLoadedState.STATE.getProject().getStatisticPages();
		assertTrue( pages.getChildCount() == 1 );

		controller.click( "#plus-button" ).click( "#untitled-page-2" ).click( "#untitled-page-1" ).click( "#plus-button" );
		assertTrue( pages.getChildCount() == 3 );

		controller.click( "#untitled-page-2" ).click( "#untitled-page-2 .tab-close-button" ).click( "#plus-button" )
				.click( "#untitled-page-1" ).click( "#untitled-page-1 .tab-close-button" );
		assertTrue( pages.getChildCount() == 2 );
		assertTrue( pages.getChildAt( 0 ).getLabel().equals( "Untitled Page 3" ) );
		assertTrue( pages.getChildAt( 1 ).getLabel().equals( "Untitled Page 4" ) );
	}
}
