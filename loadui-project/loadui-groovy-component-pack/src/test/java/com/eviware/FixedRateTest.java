package com.eviware;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.eviware.loadui.util.groovy.GroovyEnvironment;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Joiner;

public class FixedRateTest
{
	private ComponentItem component;

	@BeforeClass
	public static void classSetup()
	{
		GroovyComponentTestUtils.initialize( Joiner.on( File.separator ).join( "src", "main", "groovy" ) );
	}

	@Before
	public void setup() throws ComponentCreationException
	{
		GroovyComponentTestUtils.getDefaultBeanInjectorMocker();
		component = GroovyComponentTestUtils.createComponent( "Fixed Rate" );
		component.fireEvent( new ActionEvent( component, CanvasItem.STOP_ACTION ) );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 2 ) );

		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( GeneratorCategory.STATE_TERMINAL );
		assertThat( incoming.getLabel(), is( "Component activation" ) );

		OutputTerminal trigger = ( OutputTerminal )component.getTerminalByName( GeneratorCategory.TRIGGER_TERMINAL );
		assertThat( trigger.getLabel(), is( "Trigger Signal" ) );
	}

	@Test
	public void shouldTriggerBursts() throws InterruptedException, ExecutionException, TimeoutException
	{
		BlockingQueue<TerminalMessage> messages = ComponentTestUtils.getMessagesFrom( ( OutputTerminal )component
				.getTerminalByName( GeneratorCategory.TRIGGER_TERMINAL ) );
		component.getProperty( "burstSize" ).setValue( 3 );

		GroovyEnvironment env = GroovyComponentTestUtils.getEnvironment( component );
		env.invokeClosure( false, false, "triggerBurst" );

		assertThat( messages.poll( 1, TimeUnit.SECONDS ), notNullValue( TerminalMessage.class ) );
		assertThat( messages.poll( 1, TimeUnit.SECONDS ), notNullValue( TerminalMessage.class ) );
		assertThat( messages.poll( 1, TimeUnit.SECONDS ), notNullValue( TerminalMessage.class ) );

		TestUtils.awaitEvents( component );

		assertThat( messages.size(), is( 0 ) );

		component.getProperty( "burstSize" ).setValue( 1 );

		env.invokeClosure( false, false, "triggerBurst" );

		assertThat( messages.poll( 1, TimeUnit.SECONDS ), notNullValue( TerminalMessage.class ) );

		TestUtils.awaitEvents( component );

		assertThat( messages.size(), is( 0 ) );
	}
}