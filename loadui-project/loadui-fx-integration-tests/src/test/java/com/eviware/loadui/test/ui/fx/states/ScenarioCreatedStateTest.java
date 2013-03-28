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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.util.BeanInjector;

/**
 * Integration tests for testing the loadUI controller through its API.
 * 
 * @author henrik.olsson
 */
@Category( IntegrationTest.class )
public class ScenarioCreatedStateTest
{
	@BeforeClass
	public static void enterState() throws Exception
	{
		ScenarioCreatedState.STATE.enter();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ScenarioCreatedState.STATE.getParent().enter();
	}

	@Test
	public void shouldHaveScenario()
	{
		ProjectItem project = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace().getProjects().iterator()
				.next();
		assertThat( project.getChildren().size(), is( 1 ) );
		assertThat( project.getChildren().iterator().next().getLabel(), is( ScenarioCreatedState.SCENARIO_NAME ) );
	}
}
