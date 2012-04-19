package com.eviware;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.FlowCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;

public class DelayTest
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
		component = GroovyComponentTestUtils.createComponent( "Delay" );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 2 ) );

		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( FlowCategory.INCOMING_TERMINAL );
		assertThat( incoming.getLabel(), is( "Messages to delay" ) );

		OutputTerminal output = ( OutputTerminal )component.getTerminalByName( "output" );
		assertThat( output.getLabel(), is( "Delayed messages" ) );
	}
}
