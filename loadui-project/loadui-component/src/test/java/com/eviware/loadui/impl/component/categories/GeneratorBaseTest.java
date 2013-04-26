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
	private ComponentTestUtils ctu;

	@Before
	public void setup()
	{
		ctu = new ComponentTestUtils();
		ctu.getDefaultBeanInjectorMocker();
		component = ctu.createComponentItem();
		generatorBase = new GeneratorBase( component.getContext() )
		{
		};
		ctu.setComponentBehavior( component, generatorBase );
	}

	@Test
	public void shouldSendTriggerMessages() throws InterruptedException, ExecutionException, TimeoutException
	{
		BlockingQueue<TerminalMessage> messages = ctu.getMessagesFrom( generatorBase.getTriggerTerminal() );
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
		BlockingQueue<TerminalMessage> messages = ctu.getMessagesFrom( generatorBase.getTriggerTerminal() );

		generatorBase.getStateProperty().setValue( false );

		generatorBase.trigger();
		generatorBase.trigger();

		TestUtils.awaitEvents( component );

		assertThat( messages.isEmpty(), is( true ) );
	}
}
