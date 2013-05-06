/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.groovy.components;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.google.common.base.Joiner;

@Category( IntegrationTest.class )
public class WebRunnerTest
{
	private ComponentItem component;
	private GroovyComponentTestUtils ctu;
	
	@Before
	public void setup() throws ComponentCreationException
	{
		ctu = new GroovyComponentTestUtils();
		ctu.initialize( Joiner.on( File.separator ).join( "src", "main", "groovy" ) );
		ctu.getDefaultBeanInjectorMocker();
		component = ctu.createComponent( "Web Page Runner" );
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
