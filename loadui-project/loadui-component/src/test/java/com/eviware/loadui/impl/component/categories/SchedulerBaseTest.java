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

import com.eviware.loadui.api.component.categories.OnOffCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.eviware.loadui.util.test.TestUtils;

public class SchedulerBaseTest
{
	private SchedulerBase schedulerBase;
	private ComponentItem component;
	private ComponentTestUtils ctu;

	@Before
	public void setup()
	{
		ctu = new ComponentTestUtils();
		ctu.getDefaultBeanInjectorMocker();
		component = ctu.createComponentItem();
		schedulerBase = new SchedulerBase( component.getContext() )
		{
		};
		ctu.setComponentBehavior( component, schedulerBase );
	}

	@Test
	public void shouldSendTriggerMessages() throws InterruptedException, ExecutionException, TimeoutException
	{
		BlockingQueue<TerminalMessage> messages = ctu.getMessagesFrom( schedulerBase.getOutputTerminal() );
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

		BlockingQueue<TerminalMessage> messages = ctu.getMessagesFrom( schedulerBase.getOutputTerminal() );
		schedulerBase.sendEnabled( true );
		schedulerBase.sendEnabled( false );

		//Even when off the enable=false message should be sent!
		assertThat( messages.poll( 1, TimeUnit.SECONDS ).get( OnOffCategory.ENABLED_MESSAGE_PARAM ), is( ( Object )false ) );

		TestUtils.awaitEvents( component );

		assertThat( messages.isEmpty(), is( true ) );
	}
}
