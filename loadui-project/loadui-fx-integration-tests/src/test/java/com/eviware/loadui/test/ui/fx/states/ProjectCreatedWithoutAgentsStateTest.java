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
package com.eviware.loadui.test.ui.fx.states;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Integration tests for testing the loadUI controller through its API.
 * 
 * @author dain.nilsson
 */
@Category( IntegrationTest.class )
public class ProjectCreatedWithoutAgentsStateTest
{
	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectCreatedWithoutAgentsState.STATE.enter();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ProjectCreatedWithoutAgentsState.STATE.getParent().enter();
	}

	@Test
	public void shouldHaveProject()
	{
		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		assertThat( workspace.getProjectRefs().size(), is( 1 ) );
	}

	@Test
	public void shouldRenameProjectThoughMenuButton()
	{
		final String newProjectName = "Renamed Project";
		GUI.getController().click( "#projectRefCarousel .project-ref-view #menuButton" ).click( "#rename-item" )
				.type( newProjectName ).type( KeyCode.ENTER );
		renameTest( newProjectName );
	}

	@Test
	public void shouldRenameProjectThoughContextMenu()
	{
		String newProjectName = "Another Project";
		GUI.getController().move( "#projectRefCarousel .project-ref-view" ).click( MouseButton.SECONDARY )
				.click( "#rename-item" ).type( newProjectName ).type( KeyCode.ENTER );
		renameTest( newProjectName );
	}

	private void renameTest( final String expectedName )
	{
		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		Iterables.find( workspace.getProjectRefs(), new Predicate<ProjectRef>()
		{
			@Override
			public boolean apply( ProjectRef input )
			{
				return input.getLabel().equals( expectedName );
			}
		} );
	}

	@Test
	public void shouldCloneProjectThroughMenuButton()
	{
		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		String name = "Cloned";
		int projectCount = workspace.getProjectRefs().size();
		GUI.getController().click( "#projectRefCarousel .project-ref-view .menu-button" ).click( "#clone-item" )
				.type( name ).click( ".check-box" ).click( "#default" );
		projectCreatedTest( projectCount + 1, name );
	}

	@Test
	public void shouldCloneProjectThroughContextMenu()
	{
		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		String name = "Copy";
		int projectCount = workspace.getProjectRefs().size();
		GUI.getController().click( "#projectRefCarousel .project-ref-view .menu-button" ).click( "#clone-item" )
				.type( name ).click( ".check-box" ).click( "#default" );
		projectCreatedTest( projectCount + 1, name );
	}

	@Test
	public void shouldCreateProjectThroughContextMenu()
	{
		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		String name = "Awesome Project";
		int projectCount = workspace.getProjectRefs().size();
		GUI.getController().move( "#projectRefCarousel .prev" ).click( MouseButton.SECONDARY ).sleep( 500 )
				.click( "#create-item" ).type( name ).click( ".check-box" ).click( "#default" );
		projectCreatedTest( projectCount + 1, name );
	}

	private void projectCreatedTest( int expectedCount, final String name )
	{
		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		assertThat( workspace.getProjectRefs().size(), is( expectedCount ) );
		ProjectRef clonedRef = Iterables.find( workspace.getProjectRefs(), new Predicate<ProjectRef>()
		{
			@Override
			public boolean apply( ProjectRef input )
			{
				return input.getLabel().equals( name );
			}
		} );

		clonedRef.delete( true );
	}

}
