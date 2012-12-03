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

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.util.BeanInjector;

/**
 * Integration tests for testing the loadUI controller through its API.
 * 
 * @author henrik.olsson
 */
@Category( IntegrationTest.class )
public class AgentsCreatedStateTest
{
	@BeforeClass
	public static void enterState() throws Exception
	{
		AgentsCreatedState.STATE.enter();
	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		AgentsCreatedState.STATE.getParent().enter();
	}

	@Test
	public void shouldHaveAgents()
	{
		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		assertEquals( workspace.getAgents().size(), 3 );
	}

}
