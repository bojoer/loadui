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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.FlowCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Joiner;

public class SplitterTest
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
		component = GroovyComponentTestUtils.createComponent( "Splitter" );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 3 ) );

		System.out.println( component.getTerminals() );

		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( FlowCategory.INCOMING_TERMINAL );
		assertThat( incoming.getLabel(), is( "Incoming messages" ) );

		OutputTerminal loop = ( OutputTerminal )component.getTerminalByName( FlowCategory.OUTGOING_TERMINAL + " 1" );
		assertThat( loop.getLabel(), is( "Output Terminal 1" ) );
	}

	@Test
	public void probabilitiesShouldSumUpCorrectly() throws Exception
	{
		// 2 outputs: Change one, sum is 100%.
		component.getProperty( "numOutputs" ).setValue( 2 );
		component.getProperty( "probability0" ).setValue( 60 );
		TestUtils.awaitEvents( component );
		assertThat( ( Integer )component.getProperty( "probability1" ).getValue(), is( 40 ) );

		// 3 outputs: New output has probability 0 .
		component.getProperty( "numOutputs" ).setValue( 3 );
		TestUtils.awaitEvents( component );
		assertThat( ( Integer )component.getProperty( "probability2" ).getValue(), is( 0 ) );

		// 3 outputs: Increasing one should decrease the others, starting with the least recently changed non-zero output.
		component.getProperty( "probability2" ).setValue( 50 );
		TestUtils.awaitEvents( component );
		assertThat( ( Integer )component.getProperty( "probability0" ).getValue(), is( 50 ) );
		assertThat( ( Integer )component.getProperty( "probability1" ).getValue(), is( 0 ) );

		// 2 outputs: Sum is 100%.
		component.getProperty( "numOutputs" ).setValue( 2 );
		TestUtils.awaitEvents( component );
		assertThat( getProbabilityForOutput( 0 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 50 ) );

		// 10 outputs: All new are 0 and all old stays the same.
		component.getProperty( "numOutputs" ).setValue( 10 );
		TestUtils.awaitEvents( component );
		assertThat( getProbabilityForOutput( 0 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 2 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 3 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 4 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 5 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 6 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 7 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 8 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 9 ), is( 0 ) );

		// 1 output: It is 100%.
		component.getProperty( "numOutputs" ).setValue( 1 );
		TestUtils.awaitEvents( component );
		assertThat( getProbabilityForOutput( 0 ), is( 100 ) );
	}

	private int getProbabilityForOutput( int outputNumber )
	{
		assertThat( ( Integer )component.getProperty( "numOutputs" ).getValue(), Matchers.greaterThan( outputNumber ) );
		return ( Integer )component.getProperty( "probability" + outputNumber ).getValue();
	}
}
