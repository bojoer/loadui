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
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.util.BeanInjector;

@Category( IntegrationTest.class )
public class RootStateTest
{
	@BeforeClass
	public static void enterState() throws Exception
	{
		TestState.ROOT.enter();
	}

	@Test
	public void shouldHaveNoFailedBundles()
	{
		Bundle[] bundles = GUI.getController().getBundleContext().getBundles();

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
