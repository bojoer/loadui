package com.eviware.loadui.test.states;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.test.categories.IntegrationTest;

@Category( IntegrationTest.class )
public class WorkspaceLoadedStateTest
{
	private WorkspaceItem workspace;

	@Before
	public void enterState()
	{
		WorkspaceLoadedState.STATE.enter();
		workspace = WorkspaceLoadedState.STATE.getWorkspace();
	}

	@Test
	public void shouldHaveWorkspace()
	{
		assertThat( workspace, notNullValue() );
		workspace.setLabel( "MyTestWorkspace" );
		workspace.save();

		//Reload Workspace
		WorkspaceLoadedState.STATE.getParent().enter();
		WorkspaceLoadedState.STATE.enter();
		workspace = WorkspaceLoadedState.STATE.getWorkspace();

		assertThat( workspace.getWorkspaceFile().length(), greaterThan( 0l ) );
		assertThat( workspace.getLabel(), is( "MyTestWorkspace" ) );
	}
}
