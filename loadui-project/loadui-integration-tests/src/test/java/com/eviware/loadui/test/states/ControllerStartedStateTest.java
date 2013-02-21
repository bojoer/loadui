package com.eviware.loadui.test.states;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;

@Category( IntegrationTest.class )
public class ControllerStartedStateTest
{
	
	@Before
	public void enterState()
	{
		ControllerStartedState.STATE.enter();
	}

	@Test
	public void shouldHaveWorkspaceProvider() throws Exception
	{
		// ensure all bundles have started
		Bundle[] bundles = ControllerStartedState.STATE.controller.getBundleContext().getBundles();
		for( Bundle bundle : bundles )
			assertThat( bundle.getSymbolicName() + " is not Active or Resolved", bundle.getState(),
					anyOf( is( Bundle.ACTIVE ), is( Bundle.RESOLVED ) ) );
		
		// ensure we have a workspaceProvider
		WorkspaceProvider workspaceProvider = ControllerStartedState.STATE.getWorkspaceProviderByForce();
		assertThat( workspaceProvider, notNullValue() );
	}


}
