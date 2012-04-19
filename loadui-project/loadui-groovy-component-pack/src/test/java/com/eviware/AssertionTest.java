package com.eviware;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;

public class AssertionTest
{
	private ComponentItem component;

	@BeforeClass
	public static void classSetup()
	{
		GroovyComponentTestUtils.initialize( "src/main/groovy" );
	}

	@Before
	public void setup() throws ComponentCreationException
	{
		GroovyComponentTestUtils.getDefaultBeanInjectorMocker();
		component = GroovyComponentTestUtils.createComponent( "Assertion" );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 2 ) );

		InputTerminal input = ( InputTerminal )component.getTerminalByName( AnalysisCategory.INPUT_TERMINAL );
		assertThat( input.getLabel(), is( "Messages to assert" ) );

		OutputTerminal outputTerminal = ( OutputTerminal )component.getTerminalByName( "output" );
		assertThat( outputTerminal.getLabel(), is( "Failed messages" ) );
	}
}
