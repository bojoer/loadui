package com.eviware.loadui.impl.component.categories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.google.common.collect.ImmutableMap;

public class OutputBaseTest
{
	private BlockingQueue<TerminalMessage> outputtedMessages = new LinkedBlockingQueue<>();
	private OutputBase outputBase;
	private ComponentItem component;

	@Before
	public void setup()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		component = ComponentTestUtils.createComponentItem();
		outputBase = new OutputBase( component.getContext() )
		{
			@Override
			public void output( TerminalMessage message )
			{
				outputtedMessages.add( message );
			}
		};
		ComponentTestUtils.setComponentBehavior( component, outputBase );
	}

	@Test
	public void shouldOutputMessages() throws InterruptedException
	{
		InputTerminal input = outputBase.getInputTerminal();
		ComponentTestUtils.sendMessage( input, ImmutableMap.of( "One", "Value" ) );

		TerminalMessage message = outputtedMessages.poll( 1, TimeUnit.SECONDS );
		assertThat( message.containsKey( "One" ), is( true ) );
	}

	@Test
	public void shouldPassThroughMessages() throws InterruptedException
	{
		InputTerminal input = outputBase.getInputTerminal();
		BlockingQueue<TerminalMessage> messages = ComponentTestUtils.getMessagesFrom( outputBase.getOutputTerminal() );
		ComponentTestUtils.sendMessage( input, ImmutableMap.of( "Two", "Value" ) );

		TerminalMessage message = messages.poll( 1, TimeUnit.SECONDS );
		assertThat( message.containsKey( "Two" ), is( true ) );
	}
}
