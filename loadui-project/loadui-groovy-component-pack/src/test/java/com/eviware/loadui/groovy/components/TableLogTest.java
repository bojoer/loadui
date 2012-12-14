package com.eviware.loadui.groovy.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.OutputCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.google.common.base.Joiner;

public class TableLogTest
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
		component = GroovyComponentTestUtils.createComponent( "Table Log" );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 2 ) );

		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( OutputCategory.INPUT_TERMINAL );
		assertThat( incoming.getLabel(), is( "Data to output" ) );

		OutputTerminal output = ( OutputTerminal )component.getTerminalByName( OutputCategory.OUTPUT_TERMINAL );
		assertThat( output.getLabel(), is( "Passed through messages" ) );
	}
}
