package com.eviware.loadui.groovy.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticVariable.Mutable;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.google.common.base.Joiner;

public class ScriptRunnerTest
{
	private ComponentItem component;

	@BeforeClass
	public static void classSetup()
	{
		GroovyComponentTestUtils.initialize( Joiner.on( File.separator ).join( "src", "main", "groovy" ) );
	}

	@Before
	@SuppressWarnings( "unchecked" )
	public void setup() throws ComponentCreationException
	{
		GroovyComponentTestUtils.getDefaultBeanInjectorMocker();

		ComponentItem componentSpy = spy( ComponentTestUtils.createComponentItem() );
		ComponentContext contextSpy = spy( componentSpy.getContext() );
		doReturn( contextSpy ).when( componentSpy ).getContext();

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
		component = GroovyComponentTestUtils.createComponent( "Script Runner", componentSpy );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 3 ) );

		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( RunnerCategory.TRIGGER_TERMINAL );
		assertThat( incoming.getLabel(), is( "Trigger Input" ) );

		OutputTerminal result = ( OutputTerminal )component.getTerminalByName( RunnerCategory.RESULT_TERMINAL );
		assertThat( result.getLabel(), is( "Results" ) );
	}
}
