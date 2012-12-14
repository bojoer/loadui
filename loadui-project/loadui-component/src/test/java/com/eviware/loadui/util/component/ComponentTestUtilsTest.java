package com.eviware.loadui.util.component;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import com.eviware.loadui.api.events.TerminalEvent;
import com.eviware.loadui.api.events.TerminalMessageEvent;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalHolder;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.terminal.OutputTerminalImpl;
import com.eviware.loadui.impl.terminal.TerminalMessageImpl;
import com.eviware.loadui.util.test.BeanInjectorMocker;
import com.google.common.collect.ImmutableMap;

public class ComponentTestUtilsTest
{
	@Test
	public void shouldReturnBeanInjectorMocker()
	{
		assertThat( ComponentTestUtils.getDefaultBeanInjectorMocker(), is( BeanInjectorMocker.class ) );
	}

	@Test
	public void shouldCreateComponentItem()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		ComponentItem component = ComponentTestUtils.createComponentItem();
		assertNotNull( component.getContext() );
	}

	@Test
	public void shouldSendMessage()
	{
		ComponentItem component = mock( ComponentItem.class );
		InputTerminal terminal = mock( InputTerminal.class );
		when( terminal.getTerminalHolder() ).thenReturn( component );

		ComponentTestUtils.sendMessage( terminal, ImmutableMap.of( "Test", "Test" ) );

		verify( component ).handleTerminalEvent( any( InputTerminal.class ), any( TerminalEvent.class ) );
	}

	@Test
	public void shouldGetMessagesFromTerminal() throws InterruptedException
	{
		OutputTerminal terminal = new OutputTerminalImpl( mock( TerminalHolder.class ), "output", "output", "output" );
		BlockingQueue<TerminalMessage> messages = ComponentTestUtils.getMessagesFrom( terminal );

		TerminalMessageImpl terminalMessage = new TerminalMessageImpl( new DefaultConversionService() );
		terminalMessage.put( "Test", "TestValue" );
		terminal.fireEvent( new TerminalMessageEvent( terminal, terminalMessage ) );

		TerminalMessage message = messages.poll( 1, TimeUnit.SECONDS );
		assertThat( ( String )message.get( "Test" ), is( "TestValue" ) );
	}
}
