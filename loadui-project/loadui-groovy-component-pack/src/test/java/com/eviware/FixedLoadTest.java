package com.eviware;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;

public class FixedLoadTest
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
		component = GroovyComponentTestUtils.createComponent( "Fixed Load" );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 3 ) );

		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( GeneratorCategory.STATE_TERMINAL );
		assertThat( incoming.getLabel(), is( "Component activation" ) );

		InputTerminal feedback = ( InputTerminal )component.getTerminalByName( "Sample Count" );
		assertThat( feedback.getLabel(), is( "Currently running feedback" ) );

		OutputTerminal trueTerminal = ( OutputTerminal )component.getTerminalByName( GeneratorCategory.TRIGGER_TERMINAL );
		assertThat( trueTerminal.getLabel(), is( "Trigger Signal" ) );
	}
}
