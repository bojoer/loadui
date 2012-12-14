package com.eviware.loadui.test.ui.fx.states;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.ui.fx.util.test.TestFX;

@Category( IntegrationTest.class )
public class LastResultOpenedStateTest
{
	@BeforeClass
	public static void enterState() throws Exception
	{
		LastResultOpenedState.STATE.enter();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		LastResultOpenedState.STATE.getParent().enter();
	}

	@Test
	public void shouldHaveAnalysisView() throws Exception
	{
		TestFX.find( ".analysis-view" );
	}
}
