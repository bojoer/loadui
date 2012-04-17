package com.eviware;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.FlowCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.test.ComponentTestUtils;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.collect.ImmutableMap;

public class ConditionTest
{
	private ComponentItem component;

	@Before
	public void setup() throws ComponentCreationException
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		component = ComponentTestUtils.createComponent( "Condition" );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 3 ) );

		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( FlowCategory.INCOMING_TERMINAL );
		assertThat( incoming.getLabel(), is( "Incoming messages" ) );

		OutputTerminal trueTerminal = ( OutputTerminal )component.getTerminalByName( "trueOutput" );
		assertThat( trueTerminal.getLabel(), is( "True" ) );

		OutputTerminal falseTerminal = ( OutputTerminal )component.getTerminalByName( "falseOutput" );
		assertThat( falseTerminal.getLabel(), is( "False" ) );
	}

	@Test
	public void shouldDelegateLikeFunction() throws InterruptedException, ExecutionException, TimeoutException
	{
		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( FlowCategory.INCOMING_TERMINAL );
		OutputTerminal trueTerminal = ( OutputTerminal )component.getTerminalByName( "trueOutput" );
		OutputTerminal falseTerminal = ( OutputTerminal )component.getTerminalByName( "falseOutput" );

		OutputTerminal dummyOut = mock( OutputTerminal.class );
		InputTerminal dummyIn = mock( InputTerminal.class );
		when( dummyIn.likes( dummyOut ) ).thenReturn( true );

		//Test some combinations, should have OR behavior:
		//0, 0
		assertThat( incoming.likes( dummyOut ), is( false ) );

		//1, 0
		Connection connection1 = trueTerminal.connectTo( dummyIn );
		assertThat( incoming.likes( dummyOut ), is( true ) );

		//1, 1
		Connection connection2 = falseTerminal.connectTo( dummyIn );
		assertThat( incoming.likes( dummyOut ), is( true ) );

		//0, 1
		connection1.disconnect();
		assertThat( incoming.likes( dummyOut ), is( true ) );

		//0, 0
		connection2.disconnect();
		assertThat( incoming.likes( dummyOut ), is( false ) );
	}

	@Test
	public void shouldDirectBasedOnSimpleCondition() throws Exception
	{
		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( FlowCategory.INCOMING_TERMINAL );
		OutputTerminal trueTerminal = ( OutputTerminal )component.getTerminalByName( "trueOutput" );
		OutputTerminal falseTerminal = ( OutputTerminal )component.getTerminalByName( "falseOutput" );

		BlockingQueue<TerminalMessage> trueMessages = ComponentTestUtils.getMessagesFrom( trueTerminal );
		BlockingQueue<TerminalMessage> falseMessages = ComponentTestUtils.getMessagesFrom( falseTerminal );

		component.getProperty( "valueName" ).setValue( "TestValue" );
		component.getProperty( "min" ).setValue( 5 );
		component.getProperty( "max" ).setValue( 10 );

		ComponentTestUtils.sendMessage( incoming, ImmutableMap.of( "TestValue", 5 ) );
		TerminalMessage message = trueMessages.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( "TestValue" ), is( ( Object )5 ) );

		ComponentTestUtils.sendMessage( incoming, ImmutableMap.of( "TestValue", 11 ) );
		message = falseMessages.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( "TestValue" ), is( ( Object )11 ) );
	}

	@Test
	public void shouldDirectBasedOnAdvancedCondition() throws Exception
	{
		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( FlowCategory.INCOMING_TERMINAL );
		OutputTerminal trueTerminal = ( OutputTerminal )component.getTerminalByName( "trueOutput" );
		OutputTerminal falseTerminal = ( OutputTerminal )component.getTerminalByName( "falseOutput" );

		BlockingQueue<TerminalMessage> trueMessages = ComponentTestUtils.getMessagesFrom( trueTerminal );
		BlockingQueue<TerminalMessage> falseMessages = ComponentTestUtils.getMessagesFrom( falseTerminal );

		component.getProperty( "advancedMode" ).setValue( true );
		component.getProperty( "condition" ).setValue( "TestValue == 7" );
		TestUtils.awaitEvents( component );

		ComponentTestUtils.sendMessage( incoming, ImmutableMap.of( "TestValue", 7 ) );
		TerminalMessage message = trueMessages.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( "TestValue" ), is( ( Object )7 ) );

		ComponentTestUtils.sendMessage( incoming, ImmutableMap.of( "ToastValue", 10 ) );
		message = falseMessages.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( "ToastValue" ), is( ( Object )10 ) );
	}
}
