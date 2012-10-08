package com.eviware.loadui.test.ui.fx;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javafx.scene.input.KeyCode;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedState;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.BeanInjector;

@Category( IntegrationTest.class )
public class ProjectPlaybackTest
{
	private static TestFX controller;

	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedState.STATE.enter();
		controller = GUI.getController();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ProjectLoadedState.STATE.getParent().enter();
	}

	@Test
	public void shouldPlayAndStop() throws Exception
	{
		controller.click( ".project-playback-panel #play-button" ).sleep( 5000 );

		Collection<? extends ProjectItem> projects = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace()
				.getProjects();
		ProjectItem project = projects.iterator().next();
		assertTrue( project.isRunning() );

		controller.click( ".project-playback-panel #play-button" ).sleep( 4000 );
		assertTrue( !project.isRunning() );
	}

	@Test
	public void shouldStopOnLimit() throws Exception
	{
		ProjectItem project = ProjectLoadedState.STATE.getProject();

		controller.click( "#set-limits" ).click( "#time-limit" ).press( KeyCode.CONTROL, KeyCode.A )
				.release( KeyCode.CONTROL, KeyCode.A ).sleep( 100 ).type( "6" ).sleep( 100 ).click( "#default" )
				.sleep( 1000 ).click( ".project-playback-panel #play-button" ).sleep( 4000 );
		assertTrue( project.isRunning() );

		controller.sleep( 9000 );
		assertTrue( !project.isRunning() );
	}
}
