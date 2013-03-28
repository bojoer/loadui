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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.google.common.collect.ImmutableMap;

public class AnalysisBaseTest
{
	private BlockingQueue<TerminalMessage> analyzedMessages = new LinkedBlockingQueue<>();
	private AnalysisBase analysisBase;
	private ComponentItem component;

	@Before
	public void setup()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();
		component = ComponentTestUtils.createComponentItem();
		analysisBase = new AnalysisBase( component.getContext() )
		{
			@Override
			public void analyze( TerminalMessage message )
			{
				analyzedMessages.add( message );
			}
		};
		ComponentTestUtils.setComponentBehavior( component, analysisBase );
	}

	@Test
	public void shouldAnalyseIncomingMessage() throws InterruptedException
	{
		InputTerminal input = analysisBase.getInputTerminal();

		ComponentTestUtils.sendMessage( input, ImmutableMap.<String, Object> of( "Key", "Value" ) );

		TerminalMessage message = analyzedMessages.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( "Key" ), is( ( Object )"Value" ) );
		assertThat( message.size(), is( 1 ) );
	}
}
