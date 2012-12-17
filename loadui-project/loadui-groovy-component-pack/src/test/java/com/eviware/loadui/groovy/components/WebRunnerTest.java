package com.eviware.loadui.groovy.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.google.common.base.Joiner;

public class WebRunnerTest
{
	private ComponentItem component;

	@BeforeClass
	public static void classSetup()
	{
		GroovyComponentTestUtils.initialize( Joiner.on( File.separator ).join( "src", "main", "groovy" ) );
	}

	@Before
	public void setup() throws ComponentCreationException
	{
		GroovyComponentTestUtils.getDefaultBeanInjectorMocker();
		component = GroovyComponentTestUtils.createComponent( "Web Page Runner" );
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
