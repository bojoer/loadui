package com.eviware.loadui.impl.component.categories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.eviware.loadui.util.test.TestUtils;

public class GeneratorBaseTest
{
	private GeneratorBase generatorBase;
	private ComponentItem component;

	@Before
	public void setup()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		component = ComponentTestUtils.createComponentItem();
		generatorBase = new GeneratorBase( component.getContext() )
		{
		};
		ComponentTestUtils.setComponentBehavior( component, generatorBase );
	}

	@Test
	public void shouldSendTriggerMessages() throws InterruptedException, ExecutionException, TimeoutException
	{
		BlockingQueue<TerminalMessage> messages = ComponentTestUtils.getMessagesFrom( generatorBase.getTriggerTerminal() );
		generatorBase.trigger();

		assertThat(
				messages.poll( 1, TimeUnit.SECONDS ).containsKey( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM ),
				is( true ) );

		generatorBase.trigger();
		generatorBase.trigger();

		assertThat(
				messages.poll( 1, TimeUnit.SECONDS ).containsKey( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM ),
				is( true ) );
		assertThat(
				messages.poll( 1, TimeUnit.SECONDS ).containsKey( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM ),
				is( true ) );

		TestUtils.awaitEvents( component );

		assertThat( messages.isEmpty(), is( true ) );
	}

	@Test
	public void shouldNotSendTriggerMessagesWhenOff() throws InterruptedException, ExecutionException, TimeoutException
	{
		BlockingQueue<TerminalMessage> messages = ComponentTestUtils.getMessagesFrom( generatorBase.getTriggerTerminal() );

		generatorBase.getStateProperty().setValue( false );

		generatorBase.trigger();
		generatorBase.trigger();

		TestUtils.awaitEvents( component );

		assertThat( messages.isEmpty(), is( true ) );
	}
}
