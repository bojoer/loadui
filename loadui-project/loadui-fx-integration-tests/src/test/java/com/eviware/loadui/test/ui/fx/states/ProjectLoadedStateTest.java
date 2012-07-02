package com.eviware.loadui.test.ui.fx.states;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import javafx.scene.Node;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedState;

@Category( IntegrationTest.class )
public class ProjectLoadedStateTest
{
	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedState.STATE.enter();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ProjectLoadedState.STATE.getParent().enter();
	}

	@Test
	public void shouldHaveProjectView() throws Exception
	{
		Node projectView = GUI.getStage().getScene().lookup( ".project-view" );
		assertThat( projectView, not( nullValue() ) );
	}
}
