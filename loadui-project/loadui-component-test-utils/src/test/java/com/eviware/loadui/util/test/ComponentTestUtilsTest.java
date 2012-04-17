package com.eviware.loadui.util.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.events.TerminalEvent;
import com.eviware.loadui.api.events.TerminalMessageEvent;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalHolder;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.terminal.OutputTerminalImpl;
import com.eviware.loadui.impl.terminal.TerminalMessageImpl;
import com.google.common.collect.ImmutableMap;

public class ComponentTestUtilsTest
{
	@Test
	public void shouldReturnBeanInjectorMocker()
	{
		assertThat( ComponentTestUtils.getDefaultBeanInjectorMocker(), is( BeanInjectorMocker.class ) );
	}

	@Test
	public void shouldCreateComponent() throws ComponentCreationException, IOException
	{
		File scriptDir = new File( "target", "scripts" );
		scriptDir.mkdirs();

		File scriptFile = new File( scriptDir, "Test.groovy" );
		scriptFile.createNewFile();

		ComponentTestUtils.initialize( scriptDir.getPath() );
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		ComponentItem component = ComponentTestUtils.createComponent( "Test" );

		assertThat( component.getBehavior().getClass().getSimpleName(), startsWith( "Groovy" ) );
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
