/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;

/**
 * Integration tests for testing the loadUI controller through its API.
 * 
 * @author dain.nilsson
 */
public class ControllerTest
{
	private static ControllerWrapper controller;
	private static WorkspaceProvider workspaceProvider;

	@BeforeClass
	public static void startController() throws Exception
	{
		controller = new ControllerWrapper();
	}

	@AfterClass
	public static void stopController() throws Exception
	{
		controller.stop();
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void shouldHaveNoFailedBundles()
	{
		Bundle[] bundles = controller.getBundleContext().getBundles();
		assertThat( bundles.length, greaterThanOrEqualTo( 43 ) );
		for( Bundle bundle : bundles )
			assertThat( bundle.getSymbolicName() + " is not Active or Resolved", bundle.getState(), anyOf(
					is( Bundle.ACTIVE ), is( Bundle.RESOLVED ) ) );
	}

	@Test
	public void shouldHaveWorkspaceProvider() throws Exception
	{
		BundleContext context = controller.getBundleContext();

		ServiceReference ref = null;
		for( int tries = 100; ref == null; tries-- )
		{
			ref = context.getServiceReference( WorkspaceProvider.class.getName() );
			Thread.sleep( 100 );
		}
		Object service = context.getService( ref );
		assertThat( service, notNullValue() );

		workspaceProvider = ( WorkspaceProvider )service;
		assertThat( workspaceProvider, notNullValue() );
	}

	@Test
	public void createAndTestWorkspace()
	{
		File workspaceFile = new File( "target/workspace.xml" );
		if( workspaceFile.exists() )
			workspaceFile.delete();

		WorkspaceItem workspace = workspaceProvider.loadWorkspace( workspaceFile );
		assertThat( workspace, notNullValue() );
		workspace.setLabel( "MyTestWorkspace" );

		workspace.save();
		assertThat( workspaceFile.length() > 0, is( true ) );

		workspace.release();
		workspace = workspaceProvider.loadWorkspace( workspaceFile );
		assertThat( workspace, notNullValue() );
		assertThat( workspace.getLabel(), is( "MyTestWorkspace" ) );
	}
}
