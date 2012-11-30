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
		assertThat( component.getTerminals().size(), is( 2 ) );

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

	@Test
	public void changingKnobValueFirstTimeShouldCauseNearestKnobToCompensate() throws Exception
	{
		final int knobsCount = 4;
		component.getProperty( "numOutputs" ).setValue( knobsCount );
		TestUtils.awaitEvents( component );

		// pre-condition: first knob is 100, others are all 0
		assertThat( getProbabilityForOutput( 0 ), is( 100 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 2 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 3 ), is( 0 ) );

		// change one of them, should take off the value of the first knob
		component.getProperty( "probability1" ).setValue( 50 );
		TestUtils.awaitEvents( component );

		assertThat( getProbabilityForOutput( 0 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 2 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 3 ), is( 0 ) );

	}

	@Test
	public void changingKnobValueShouldCausePreviouslyChangedKnobToCompensate() throws Exception
	{
		final int knobsCount = 4;
		component.getProperty( "numOutputs" ).setValue( knobsCount );
		TestUtils.awaitEvents( component );

		// pre-condition: first knob is 100, others are all 0
		assertThat( getProbabilityForOutput( 0 ), is( 100 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 2 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 3 ), is( 0 ) );

		// change one of them, should take off the value of the first knob
		component.getProperty( "probability1" ).setValue( 50 );
		TestUtils.awaitEvents( component );

		// change a second one, should take off the value from the previously changed one
		component.getProperty( "probability2" ).setValue( 25 );
		TestUtils.awaitEvents( component );

		printValues();

		// after setting p2 to 25, should take this 25 from last changed which was p1
		assertThat( getProbabilityForOutput( 0 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 25 ) );
		assertThat( getProbabilityForOutput( 2 ), is( 25 ) );
		assertThat( getProbabilityForOutput( 3 ), is( 0 ) );

		// now, setting p3 to 40 should cause the two previously changed knobs to go down
		component.getProperty( "probability3" ).setValue( 40 );
		TestUtils.awaitEvents( component );

		// after setting p3 to 40, p2 should go down from 25 to 0, p1 should go down from 25 to 10
		assertThat( getProbabilityForOutput( 0 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 10 ) );
		assertThat( getProbabilityForOutput( 2 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 3 ), is( 40 ) );

	}

	@Test
	public void changingAKnobToMoreThan100ShouldNotBePossible() throws Exception
	{
		component.getProperty( "numOutputs" ).setValue( 1 );
		TestUtils.awaitEvents( component );

		// pre-condition
		assertThat( getProbabilityForOutput( 0 ), is( 100 ) );

		// set an invalid value
		component.getProperty( "probability0" ).setValue( 101 );
		TestUtils.awaitEvents( component );

		assertThat( getProbabilityForOutput( 0 ), is( 100 ) );

		// set an invalid value
		component.getProperty( "probability0" ).setValue( 98712 );
		TestUtils.awaitEvents( component );

		assertThat( getProbabilityForOutput( 0 ), is( 100 ) );

	}

	@Test
	public void changingAKnobToTooSmallValueShouldNotBePossible() throws Exception
	{
		component.getProperty( "numOutputs" ).setValue( 1 );
		TestUtils.awaitEvents( component );

		// pre-condition
		assertThat( getProbabilityForOutput( 0 ), is( 100 ) );

		// set an invalid value
		component.getProperty( "probability0" ).setValue( 98 );
		TestUtils.awaitEvents( component );

		assertThat( getProbabilityForOutput( 0 ), is( 100 ) );

		// set an invalid value
		component.getProperty( "probability0" ).setValue( -98 );
		TestUtils.awaitEvents( component );

		assertThat( getProbabilityForOutput( 0 ), is( 100 ) );

	}

	@Test
	public void changingSeveralKnobValuesShouldSumUpTo100() throws Exception
	{
		final int knobsCount = 7;
		component.getProperty( "numOutputs" ).setValue( knobsCount );
		TestUtils.awaitEvents( component );

		// set knobs to absurd values
		component.getProperty( "probability0" ).setValue( 50 );
		component.getProperty( "probability1" ).setValue( 20 );
		component.getProperty( "probability2" ).setValue( 20 );
		component.getProperty( "probability3" ).setValue( 80 );
		component.getProperty( "probability4" ).setValue( 60 );
		component.getProperty( "probability5" ).setValue( 30 );
		component.getProperty( "probability6" ).setValue( 2333333 );
		TestUtils.awaitEvents( component );

		// only the first ones that sum up to 100 should be set
		assertThat( getProbabilityForOutput( 0 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 20 ) );
		assertThat( getProbabilityForOutput( 2 ), is( 20 ) );
		assertThat( getProbabilityForOutput( 3 ), is( 10 ) );
		assertThat( getProbabilityForOutput( 4 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 5 ), is( 0 ) );
		assertThat( getProbabilityForOutput( 6 ), is( 0 ) );

	}

	@Test
	public void changingSeveralKnobValuesTooHighAfterValidValuesEnteredNotAllowed() throws Exception
	{
		final int knobsCount = 7;
		component.getProperty( "numOutputs" ).setValue( knobsCount );
		TestUtils.awaitEvents( component );

		// set knobs to absurd values
		component.getProperty( "probability0" ).setValue( 50 );
		component.getProperty( "probability1" ).setValue( 10 );
		component.getProperty( "probability2" ).setValue( 10 );
		component.getProperty( "probability3" ).setValue( 10 );
		component.getProperty( "probability4" ).setValue( 10 );
		component.getProperty( "probability5" ).setValue( 5 );
		component.getProperty( "probability6" ).setValue( 5 );
		TestUtils.awaitEvents( component );

		// ensure valid values were accepted
		assertThat( getProbabilityForOutput( 0 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 10 ) );
		assertThat( getProbabilityForOutput( 2 ), is( 10 ) );
		assertThat( getProbabilityForOutput( 3 ), is( 10 ) );
		assertThat( getProbabilityForOutput( 4 ), is( 10 ) );
		assertThat( getProbabilityForOutput( 5 ), is( 5 ) );
		assertThat( getProbabilityForOutput( 6 ), is( 5 ) );

		// try to change knobs to invalid values
		component.getProperty( "probability0" ).setValue( 80 );
		component.getProperty( "probability3" ).setValue( 30 );
		component.getProperty( "probability5" ).setValue( 50 );
		TestUtils.awaitEvents( component );

		// ensure valid values were NOT accepted
		assertThat( getProbabilityForOutput( 0 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 10 ) );
		assertThat( getProbabilityForOutput( 2 ), is( 10 ) );
		assertThat( getProbabilityForOutput( 3 ), is( 10 ) );
		assertThat( getProbabilityForOutput( 4 ), is( 10 ) );
		assertThat( getProbabilityForOutput( 5 ), is( 5 ) );
		assertThat( getProbabilityForOutput( 6 ), is( 5 ) );

	}

	@Test
	public void settingNonNumericValuesNotAllowed() throws Exception
	{
		component.getProperty( "numOutputs" ).setValue( 2 );
		TestUtils.awaitEvents( component );

		// set knobs to absurd values
		component.getProperty( "probability0" ).setValue( "bogus" );
		component.getProperty( "probability1" ).setValue( 50 );
		TestUtils.awaitEvents( component );

		// values should remain as they were initially
		assertThat( getProbabilityForOutput( 0 ), is( 100 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 0 ) );

		// set knobs to valid values
		component.getProperty( "probability0" ).setValue( 50 );
		component.getProperty( "probability1" ).setValue( 50 );
		TestUtils.awaitEvents( component );

		// values should have changed
		assertThat( getProbabilityForOutput( 0 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 50 ) );

		// set knobs to absurd values
		component.getProperty( "probability0" ).setValue( "bogus" );
		component.getProperty( "probability1" ).setValue( "bad" );
		TestUtils.awaitEvents( component );

		// values should remain as they were before
		assertThat( getProbabilityForOutput( 0 ), is( 50 ) );
		assertThat( getProbabilityForOutput( 1 ), is( 50 ) );

	}

	/**
	 * Useful for debugging what the knob values end up being set to
	 */
	private void printValues()
	{
		String toPrint = "";
		for( int i = 0; true; i++ )
		{
			try
			{
				toPrint += "prob" + i + ":" + getProbabilityForOutput( i ) + " ";
			}
			catch( Error e )
			{
				break;
			}
		}
		System.out.println( toPrint );
	}

	private int getProbabilityForOutput( int outputNumber )
	{
		assertThat( ( Integer )component.getProperty( "numOutputs" ).getValue(), Matchers.greaterThan( outputNumber ) );
		return ( Integer )component.getProperty( "probability" + outputNumber ).getValue();
	}
}
