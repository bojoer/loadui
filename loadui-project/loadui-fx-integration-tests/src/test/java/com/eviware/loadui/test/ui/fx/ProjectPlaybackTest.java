/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
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
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.BeanInjector;

@Category( IntegrationTest.class )
public class ProjectPlaybackTest
{
	private static TestFX controller;

	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();
		controller = GUI.getController();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.getParent().enter();
	}

	@Test
	public void shouldPlayAndStop() throws Exception
	{
		controller.click( ".project-playback-panel .play-button" ).sleep( 5000 );

		Collection<? extends ProjectItem> projects = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace()
				.getProjects();
		ProjectItem project = projects.iterator().next();
		assertTrue( project.isRunning() );

		controller.click( ".project-playback-panel .play-button" ).sleep( 4000 );
		assertTrue( !project.isRunning() );
	}

	@Test
	public void shouldStopOnLimit() throws Exception
	{
		ProjectItem project = ProjectLoadedWithoutAgentsState.STATE.getProject();

		controller.click( "#set-limits" ).click( "#time-limit" ).press( KeyCode.CONTROL, KeyCode.A )
				.release( KeyCode.CONTROL, KeyCode.A ).sleep( 100 ).type( "6" ).sleep( 100 ).click( "#default" )
				.sleep( 1000 ).click( ".project-playback-panel .play-button" ).sleep( 4000 );
		assertTrue( project.isRunning() );

		controller.sleep( 9000 );
		assertTrue( !project.isRunning() );
	}
}
