package com.eviware.loadui.impl.component.categories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticVariable.Mutable;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.google.common.collect.ImmutableMap;

public class RunnerBaseTest
{
	private RunnerBase runnerBase;
	private ComponentItem component;
	private InputTerminal triggerTerminal;
	private OutputTerminal resultsTerminal;

	@Before
	@SuppressWarnings( "unchecked" )
	public void setup()
	{
		ComponentTestUtils.getDefaultBeanInjectorMocker();

		component = ComponentTestUtils.createComponentItem();
		ComponentItem componentSpy = spy( component );
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
				return triggerMessage;
			}

			@Override
			protected int onCancel()
			{
				return 0;
			}

		};
		ComponentTestUtils.setComponentBehavior( component, runnerBase );
		contextSpy.setNonBlocking( true );
		component = componentSpy;

		triggerTerminal = runnerBase.getTriggerTerminal();
		resultsTerminal = runnerBase.getResultTerminal();
	}

	@Test
	public void shouldSampleOnIncomingMessage() throws InterruptedException
	{
		BlockingQueue<TerminalMessage> results = ComponentTestUtils.getMessagesFrom( resultsTerminal );
		ComponentTestUtils.sendMessage( triggerTerminal,
				ImmutableMap.<String, Object> of( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM, 0 ) );

		TerminalMessage message = results.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM ), is( ( Object )0 ) );
		assertThat( message.size(), is( 3 ) );
		assertThat( runnerBase.getRequestCounter().getValue(), is( 1L ) );
		assertThat( runnerBase.getSampleCounter().getValue(), is( 1L ) );
		assertThat( runnerBase.getFailureCounter().getValue(), is( 0L ) );
	}
}
