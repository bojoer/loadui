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

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.osgi.framework.Bundle;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.util.BeanInjector;

@Category( IntegrationTest.class )
public class FXAppLoadedStateTest
{
	@BeforeClass
	public static void enterState() throws Exception
	{
		FXAppLoadedState.STATE.enter();
	}

	@Test
	public void shouldHaveNoFailedBundles()
	{
		Bundle[] bundles = GUI.getBundleContext().getBundles();

		for( Bundle bundle : bundles )
		{
			assertThat( bundle.getSymbolicName() + " is not Active or Resolved", bundle.getState(),
					anyOf( is( Bundle.ACTIVE ), is( Bundle.RESOLVED ) ) );
			System.out.println( "Bundle: " + bundle );
		}

		System.out.println( "BUNDLES: " + bundles.length );
	}

	@Test
	public void shouldHaveNoProjects()
	{
		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
		assertThat( workspace.getProjectRefs().size(), is( 0 ) );
	}
}
