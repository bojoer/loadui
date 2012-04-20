package com.eviware;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.google.common.base.Joiner;

@Deprecated
public class StatisticsTest
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
		component = GroovyComponentTestUtils.createComponent( "Statistics" );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 3 ) );

		InputTerminal input = ( InputTerminal )component.getTerminalByName( AnalysisCategory.INPUT_TERMINAL );
		assertThat( input.getLabel(), is( "Input values" ) );

		InputTerminal statistics = ( InputTerminal )component.getTerminalByName( "statistics" );
		assertThat( statistics.getLabel(), is( "Runner Statistics" ) );

		OutputTerminal outputTerminal = ( OutputTerminal )component.getTerminalByName( "output" );
		assertThat( outputTerminal.getLabel(), is( "Statistic data" ) );
	}
}
