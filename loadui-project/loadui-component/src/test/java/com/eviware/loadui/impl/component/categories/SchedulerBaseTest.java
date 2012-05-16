package com.eviware.loadui.impl.component.categories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.component.categories.OnOffCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.eviware.loadui.util.test.TestUtils;

public class SchedulerBaseTest
{
	private SchedulerBase schedulerBase;
	private ComponentItem component;

	@Before
	public void setup()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		component = ComponentTestUtils.createComponentItem();
		schedulerBase = new SchedulerBase( component.getContext() )
		{
		};
		ComponentTestUtils.setComponentBehavior( component, schedulerBase );
	}

	@Test
	public void shouldSendTriggerMessages() throws InterruptedException, ExecutionException, TimeoutException
	{
		BlockingQueue<TerminalMessage> messages = ComponentTestUtils.getMessagesFrom( schedulerBase.getOutputTerminal() );
		schedulerBase.sendEnabled( true );

		assertThat( messages.poll( 1, TimeUnit.SECONDS ).get( OnOffCategory.ENABLED_MESSAGE_PARAM ), is( ( Object )true ) );

		schedulerBase.sendEnabled( false );

		assertThat( messages.poll( 1, TimeUnit.SECONDS ).get( OnOffCategory.ENABLED_MESSAGE_PARAM ), is( ( Object )false ) );

		TestUtils.awaitEvents( component );

		assertThat( messages.isEmpty(), is( true ) );
	}

	@Test
	public void shouldNotSendMessagesWhenOff() throws InterruptedException, ExecutionException, TimeoutException
	{
		schedulerBase.getStateProperty().setValue( false );
		TestUtils.awaitEvents( component );

		BlockingQueue<TerminalMessage> messages = ComponentTestUtils.getMessagesFrom( schedulerBase.getOutputTerminal() );
		schedulerBase.sendEnabled( true );
		schedulerBase.sendEnabled( false );

		//Even when off the enable=false message should be sent!
		assertThat( messages.poll( 1, TimeUnit.SECONDS ).get( OnOffCategory.ENABLED_MESSAGE_PARAM ), is( ( Object )false ) );

		TestUtils.awaitEvents( component );

		assertThat( messages.isEmpty(), is( true ) );
	}
}
