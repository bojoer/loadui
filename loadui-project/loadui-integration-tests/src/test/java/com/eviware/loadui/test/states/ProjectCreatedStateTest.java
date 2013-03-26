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
package com.eviware.loadui.test.states;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.test.categories.IntegrationTest;

@Category( IntegrationTest.class )
public class ProjectCreatedStateTest
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
