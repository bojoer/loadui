package com.eviware.loadui.test.states;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.test.AgentTest;
import com.eviware.loadui.test.categories.IntegrationTest;

@Category( IntegrationTest.class )
public class ProjectCreatedStateTest extends AgentTest
{
	private ProjectItem project;

	@Before
	public void enterState()
	{
		ProjectCreatedState.STATE.enter();
		project = ProjectCreatedState.STATE.getProject();
	}

	@Test
	public void shouldHaveProject()
	{
		assertNotNull( project );
		assertNotNull( project.getLabel() );

		String newLabel = "A new Project name";
		project.setLabel( newLabel );

		assertThat( project.getLabel(), is( newLabel ) );
	}
}
