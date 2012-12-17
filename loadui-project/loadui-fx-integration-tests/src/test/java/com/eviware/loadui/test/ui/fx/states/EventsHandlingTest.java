/*
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.test.ui.fx.states;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javafx.scene.Node;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Predicate;

/**
 * Integration tests for testing loadUI events handling.
 * 
 * @author renato
 */
@Category( IntegrationTest.class )
public class EventsHandlingTest
{

	private static final Logger log = LoggerFactory.getLogger( EventsHandlingTest.class );

	private static final Predicate<Node> CONDITION_COMPONENT = new Predicate<Node>()
	{
		@Override
		public boolean apply( Node input )
		{
			if( input.getClass().getSimpleName().equals( "ComponentDescriptorView" ) )
			{
				System.out.println( "^^^^^^^^^^^^^^^^^^^^^^^^^FOUND ComponentDescriptorView: " + input.toString() );
				return input.toString().equals( "Fixed Rate" );
			}
			return false;
		}
	};

	private static TestFX controller;

	private static ArrayList<Node> knobs;

	@BeforeClass
	public static void enterState() throws Exception
	{

		controller = GUI.getController();

		log.info( "Asking to enter ProjectLoadedState" );
		ProjectLoadedWithoutAgentsState.STATE.enter();

	}

	@AfterClass
	public static void leaveState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.getParent().enter();
	}

	@Test
	public void testKnobExists() throws Exception
	{
		controller.click( "#Generators" ).drag( CONDITION_COMPONENT ).by( 150, -50 ).drop();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestFX.findAll( ".canvas-object-view" ).size() == 1;
			}
		} );

		System.gc();
		System.gc();
		System.gc();

		Node component = TestFX.find( ".component-view" );
		assertNotNull( component );

		log.info( "CHECKING IF THERE IS ANY KNOB AROUND" );
		knobs = new ArrayList<>( TestFX.findAll( ".knob", component ) );

		assertFalse( knobs.isEmpty() );

		Node knob = knobs.get( 0 );

		controller.drag( knob ).by( 0, -100 ).drop();

//		Knob theKnob = ( Knob )knob;
		System.out.println( "Knob class: " + knob.getClass().getName() );

//		System.out.println( "Knob's value: " + theKnob.getValue() );

	}

}
