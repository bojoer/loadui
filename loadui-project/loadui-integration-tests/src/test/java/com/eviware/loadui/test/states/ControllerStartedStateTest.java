package com.eviware.loadui.test.states;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;

@Category( IntegrationTest.class )
public class ControllerStartedStateTest
{
	private BundleContext context;

	@Before
	public void enterState()
	{
		ControllerStartedState.STATE.enter();
		context = ControllerStartedState.STATE.getBundleContext();
	}

	@Test
	public void shouldHaveWorkspaceProvider() throws Exception
	{
		//TODO: We can't use generics here until the OSGi jars stop using compilation flags that are not compatible with Java7.
		ServiceReference/* <WorkspaceProvider> */ref = null;
		for( int tries = 100; ref == null && tries > 0; tries-- )
		{
			ref = context.getServiceReference( WorkspaceProvider.class.getName() );
			Thread.sleep( 100 );
		}
		WorkspaceProvider workspaceProvider = ( WorkspaceProvider )context.getService( ref );

		assertThat( workspaceProvider, notNullValue() );
	}
}
