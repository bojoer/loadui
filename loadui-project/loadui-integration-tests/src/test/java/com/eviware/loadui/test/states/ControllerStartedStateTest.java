package com.eviware.loadui.test.states;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.AgentTest;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.util.BeanInjector;

@Category( IntegrationTest.class )
public class ControllerStartedStateTest extends AgentTest
{
	private static final Logger log = LoggerFactory.getLogger( ControllerStartedStateTest.class );

	@Before
	public void enterState()
	{
		ControllerStartedState.STATE.enter();
	}

	@Test
	public void shouldHaveWorkspaceProvider() throws Exception
	{
		Thread.sleep( 4000 );
		log.info( "Running test shouldHaveWorkspaceProvider" );
		WorkspaceProvider workspaceProvider = BeanInjector.getBean( WorkspaceProvider.class, 1000 );

		assertThat( workspaceProvider, notNullValue() );
	}
}
