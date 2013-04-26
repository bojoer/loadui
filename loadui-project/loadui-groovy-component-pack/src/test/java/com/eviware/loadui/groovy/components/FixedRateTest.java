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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
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
import com.eviware.loadui.util.groovy.GroovyEnvironment;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Joiner;

public class FixedRateTest
{
	private ComponentItem component;
	private GroovyComponentTestUtils ctu;

	@Before
	public void setup() throws ComponentCreationException
	{
		ctu = new GroovyComponentTestUtils();
		ctu.initialize( Joiner.on( File.separator ).join( "src", "main", "groovy" ) );
		ctu.getDefaultBeanInjectorMocker();
		component = ctu.createComponent( "Fixed Rate" );
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
		BlockingQueue<TerminalMessage> messages = ctu.getMessagesFrom( ( OutputTerminal )component
				.getTerminalByName( GeneratorCategory.TRIGGER_TERMINAL ) );
		component.getProperty( "burstSize" ).setValue( 3 );

		GroovyEnvironment env = ctu.getEnvironment( component );
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
