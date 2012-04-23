package com.eviware.loadui.impl.component.categories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticVariable.Mutable;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.model.ComponentItemImpl;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.google.common.collect.ImmutableMap;

public class RunnerBaseTest
{
	private BlockingQueue<TerminalMessage> sampleMessages = new LinkedBlockingQueue<TerminalMessage>();
	private RunnerBase runnerBase;
	private ComponentItemImpl component;

	@Before
	@SuppressWarnings( "unchecked" )
	public void setup()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();

		component = ComponentTestUtils.createComponentItem();
		ComponentItemImpl componentSpy = spy( component );
		ComponentContext contextSpy = spy( component.getContext() );
		doReturn( contextSpy ).when( componentSpy ).getContext();
		doReturn( componentSpy ).when( contextSpy ).getComponent();

		final Mutable mockVariable = mock( StatisticVariable.Mutable.class );
		when( mockVariable.getStatisticHolder() ).thenReturn( componentSpy );
		@SuppressWarnings( "rawtypes" )
		final Statistic statisticMock = mock( Statistic.class );
		when( statisticMock.getStatisticVariable() ).thenReturn( mockVariable );
		when( mockVariable.getStatistic( anyString(), anyString() ) ).thenReturn( statisticMock );
		doReturn( mockVariable ).when( contextSpy ).addStatisticVariable( anyString(), anyString(),
				Matchers.<String> anyVararg() );
		doReturn( mockVariable ).when( contextSpy ).addListenableStatisticVariable( anyString(), anyString(),
				Matchers.<String> anyVararg() );

		runnerBase = new RunnerBase( contextSpy )
		{
			@Override
			protected TerminalMessage sample( TerminalMessage triggerMessage, Object sampleId )
					throws SampleCancelledException
			{
				sampleMessages.add( triggerMessage );
				return triggerMessage;
			}

			@Override
			protected int onCancel()
			{
				return 0;
			}

		};
		component.setBehavior( runnerBase );
		contextSpy.setNonBlocking( true );
		component = componentSpy;
	}

	@Test
	public void shouldSampleOnIncomingMessage() throws InterruptedException
	{
		InputTerminal triggerTerminal = runnerBase.getTriggerTerminal();

		ComponentTestUtils.sendMessage( triggerTerminal,
				ImmutableMap.<String, Object> of( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM, 0 ) );

		TerminalMessage message = sampleMessages.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM ), is( ( Object )0 ) );
		assertThat( message.size(), is( 3 ) );
	}
}
