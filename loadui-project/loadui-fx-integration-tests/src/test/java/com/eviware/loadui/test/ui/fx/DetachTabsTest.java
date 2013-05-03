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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.test.TestUtils;

/**
 * 
 * @author OSTEN
 * 
 */
@Category( IntegrationTest.class )
public class DetachTabsTest
{

	private static TestFX controller;

	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();
		controller = GUI.getController();

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestFX.findAll( ".detachable-tab" ).size() > 1;
			}
		} );
	}

	@Test
	public void shouldDetachAndReattachWorkspace() throws Exception
	{

		controller.click( "#designTab" ).click( "#designTab #detachButton" );

		try
		{
			TestUtils.awaitCondition( new Callable<Boolean>()
			{
				@Override
				public Boolean call() throws Exception
				{
					return TestFX.findAll( ".detached-content .project-canvas-view" ).size() == 1;
				}
			}, 2 );
		}
		catch( TimeoutException e )
		{
			fail( "cannot create project-canvas-view" );
		}
		//Check so that 
		assertThat( TestFX.findAll( ".detached-content .project-canvas-view" ).size(), is( 1 ) );

		controller.closeCurrentWindow();
	}

	@Test
	public void shouldDetachAndReattachStatistics() throws Exception
	{

		controller.click( "#statsTab" ).click( "#statsTab #detachButton" );

		try
		{
			TestUtils.awaitCondition( new Callable<Boolean>()
			{
				@Override
				public Boolean call() throws Exception
				{
					return TestFX.findAll( ".detached-content .analysis-view" ).size() == 1;
				}
			}, 2 );
		}
		catch( TimeoutException e )
		{
			fail( "cannot create analysis-view" );
		}

		//Check so that 
		assertThat( TestFX.findAll( ".detached-content .analysis-view" ).size(), is( 1 ) );

		controller.closeCurrentWindow();
	}

	@AfterClass
	public static void cleanup()
	{
		controller = null;
		ProjectLoadedWithoutAgentsState.STATE.getParent().enter();
	}

}
