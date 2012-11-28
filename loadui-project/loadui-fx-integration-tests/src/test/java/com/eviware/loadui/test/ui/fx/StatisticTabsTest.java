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

		//		controller.click( ".project-playback-panel #play-button" );
	}

}
