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
