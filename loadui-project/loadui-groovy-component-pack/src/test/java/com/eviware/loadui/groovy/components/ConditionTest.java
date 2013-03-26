/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.groovy.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.FlowCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

public class ConditionTest
{
	private ComponentItem component;

	@BeforeClass
	public static void classSetup()
	{
		GroovyComponentTestUtils.getDefaultBeanInjectorMocker();
		GroovyComponentTestUtils.initialize( Joiner.on( File.separator ).join( "src", "main", "groovy" ) );
	}

	@Before
	public void setup() throws ComponentCreationException
	{
		component = GroovyComponentTestUtils.createComponent( "Condition" );
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
	public void shouldDirectBasedOnSimpleCondition() throws Exception
	{
		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( FlowCategory.INCOMING_TERMINAL );
		OutputTerminal trueTerminal = ( OutputTerminal )component.getTerminalByName( "trueOutput" );
		OutputTerminal falseTerminal = ( OutputTerminal )component.getTerminalByName( "falseOutput" );

		BlockingQueue<TerminalMessage> trueMessages = GroovyComponentTestUtils.getMessagesFrom( trueTerminal );
		BlockingQueue<TerminalMessage> falseMessages = GroovyComponentTestUtils.getMessagesFrom( falseTerminal );

		component.getProperty( "valueName" ).setValue( "TestValue" );
		component.getProperty( "min" ).setValue( 5 );
		component.getProperty( "max" ).setValue( 10 );

		GroovyComponentTestUtils.sendMessage( incoming, ImmutableMap.of( "TestValue", 5 ) );
		TerminalMessage message = trueMessages.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( "TestValue" ), is( ( Object )5 ) );

		GroovyComponentTestUtils.sendMessage( incoming, ImmutableMap.of( "TestValue", 11 ) );
		message = falseMessages.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( "TestValue" ), is( ( Object )11 ) );
	}

	@Test
	public void shouldDirectBasedOnAdvancedCondition() throws Exception
	{
		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( FlowCategory.INCOMING_TERMINAL );
		OutputTerminal trueTerminal = ( OutputTerminal )component.getTerminalByName( "trueOutput" );
		OutputTerminal falseTerminal = ( OutputTerminal )component.getTerminalByName( "falseOutput" );

		BlockingQueue<TerminalMessage> trueMessages = GroovyComponentTestUtils.getMessagesFrom( trueTerminal );
		BlockingQueue<TerminalMessage> falseMessages = GroovyComponentTestUtils.getMessagesFrom( falseTerminal );

		component.getProperty( "advancedMode" ).setValue( true );
		component.getProperty( "condition" ).setValue( "TestValue == 7" );
		TestUtils.awaitEvents( component );

		GroovyComponentTestUtils.sendMessage( incoming, ImmutableMap.of( "TestValue", 7 ) );
		TerminalMessage message = trueMessages.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( "TestValue" ), is( ( Object )7 ) );

		GroovyComponentTestUtils.sendMessage( incoming, ImmutableMap.of( "ToastValue", 10 ) );
		message = falseMessages.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( "ToastValue" ), is( ( Object )10 ) );
	}
}
