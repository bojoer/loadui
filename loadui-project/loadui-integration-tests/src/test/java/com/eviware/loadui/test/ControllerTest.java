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
package com.eviware.loadui.test;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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

	private static void setWorkspaceProvider( WorkspaceProvider provider )
	{
		workspaceProvider = provider;
	}

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

	@Test
	public void shouldHaveNoFailedBundles()
	{
		Bundle[] bundles = controller.getBundleContext().getBundles();

		for( Bundle bundle : bundles )
		{
			assertThat( bundle.getSymbolicName() + " is not Active or Resolved", bundle.getState(),
					anyOf( is( Bundle.ACTIVE ), is( Bundle.RESOLVED ) ) );
			System.out.println( "Bundle: " + bundle );
		}

		System.out.println( "BUNDLES: " + bundles.length );
	}

	@Test
	public void shouldHaveWorkspaceProvider() throws Exception
	{
		BundleContext context = controller.getBundleContext();

		ServiceReference<WorkspaceProvider> ref = null;
		for( int tries = 100; ref == null && tries > 0; tries-- )
		{
			ref = context.getServiceReference( WorkspaceProvider.class );
			Thread.sleep( 100 );
		}
		WorkspaceProvider service = context.getService( ref );
		assertThat( service, notNullValue() );

		setWorkspaceProvider( service );
		assertThat( workspaceProvider, notNullValue() );
	}

	@Test
	public void createAndTestWorkspace() throws IOException
	{
		File workspaceFile = new File( "target/workspace.xml" );
		if( workspaceFile.exists() )
			if( !workspaceFile.delete() )
				throw new IOException( "Unable to delete: " + workspaceFile );

		WorkspaceItem workspace = workspaceProvider.loadWorkspace( workspaceFile );
		assertThat( workspace, notNullValue() );
		workspace.setLabel( "MyTestWorkspace" );

		workspace.save();
		assertThat( workspaceFile.length(), greaterThan( 0l ) );

		workspace.release();
		workspace = workspaceProvider.loadWorkspace( workspaceFile );
		assertThat( workspace, notNullValue() );
		assertThat( workspace.getLabel(), is( "MyTestWorkspace" ) );
	}
}
