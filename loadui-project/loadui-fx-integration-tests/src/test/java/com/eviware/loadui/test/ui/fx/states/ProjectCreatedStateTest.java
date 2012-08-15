/*
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.test.ui.fx.states;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.stage.PopupWindow;

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
public class ProjectCreatedStateTest
{
	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectCreatedState.STATE.enter();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ProjectCreatedState.STATE.getParent().enter();
	}

	@Test
	public void shouldHaveProject()
	{
		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		assertThat( workspace.getProjectRefs().size(), is( 1 ) );
	}

	@Test
	public void shouldRenameProject()
	{
		GUI.getController().click( "#projectRefCarousel .project-ref-view .menu-button" ).target( PopupWindow.class )
				.click( "#rename" ).type( "Renamed Project" ).type( KeyCode.ENTER );

		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		Iterables.find( workspace.getProjectRefs(), new Predicate<ProjectRef>()
		{
			@Override
			public boolean apply( ProjectRef input )
			{
				return input.getLabel().equals( "Renamed Project" );
			}
		} );
	}

	@Test
	public void shouldCloneProject()
	{
		GUI.getController().click( "#projectRefCarousel .project-ref-view .menu-button" ).target( PopupWindow.class )
				.click( "#clone" ).type( "Copy" ).type( KeyCode.TAB ).type( KeyCode.TAB ).type( KeyCode.TAB )
				.type( KeyCode.SPACE ).type( KeyCode.TAB ).type( KeyCode.ENTER );

		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		assertThat( workspace.getProjectRefs().size(), is( 2 ) );
		ProjectRef clonedRef = Iterables.find( workspace.getProjectRefs(), new Predicate<ProjectRef>()
		{
			@Override
			public boolean apply( ProjectRef input )
			{
				return input.getLabel().equals( "Copy" );
			}
		} );

		clonedRef.delete( true );
	}

	public void traverse( Node node, int indent )
	{
		StringBuilder indentation = new StringBuilder();
		for( int i = 0; i < indent; i++ )
		{
			indentation.append( "  " );
		}
		System.out.println( indentation + "Node id: " + node.getId() + ", class: " + node.getClass().getSimpleName()
				+ " StyleClass: " + node.getStyleClass() );
		if( node instanceof Parent )
		{
			System.out.println( indentation + "Children:" );
			for( Node child : ( ( Parent )node ).getChildrenUnmodifiable() )
			{
				traverse( child, indent + 1 );
			}
		}
	}
}
