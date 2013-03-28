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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.ControllerWrapper;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.util.BeanInjector;

public class ControllerStartedState extends TestState
{
	public static final ControllerStartedState STATE = new ControllerStartedState();

	public ControllerWrapper controller;

	private ControllerStartedState()
	{
		super( "Controller Started", TestState.ROOT );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		controller = new ControllerWrapper();
		BeanInjector.setBundleContext( controller.getBundleContext() );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		controller.stop();
	}

	public BundleContext getBundleContext()
	{
		return controller.getBundleContext();
	}
	
	public WorkspaceProvider getWorkspaceProviderByForce() throws InterruptedException
	{
		//TODO: We can't use generics here until the OSGi jars stop using compilation flags that are not compatible with Java7.
		BundleContext context = ControllerStartedState.STATE.getBundleContext();
		ServiceReference/* <WorkspaceProvider> */ref = null;
		for( int tries = 100; ref == null && tries > 0; tries-- )
		{
			ref = context.getServiceReference( WorkspaceProvider.class.getName() );
			Thread.sleep( 100 );
		}
		WorkspaceProvider workspaceProvider = ( WorkspaceProvider )context.getService( ref );
		return workspaceProvider;
	}
}
