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
package com.eviware.loadui.test.ui.fx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javafx.scene.Node;
import javafx.stage.Stage;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * 
 * @author renato
 * 
 */
@Category( IntegrationTest.class )
public class ExecutionTest
{

	private static TestFX controller;

	private static final Predicate<Node> FIXED_RATE = new Predicate<Node>()
	{
		@Override
		public boolean apply( Node input )
		{
			if( input.getClass().getSimpleName().equals( "ComponentDescriptorView" ) )
			{
				return input.toString().equals( "Fixed Rate" );
			}
			return false;
		}
	};

	private static final Predicate<Node> WEB_PAGE_RUNNER = new Predicate<Node>()
	{
		@Override
		public boolean apply( Node input )
		{
			if( input.getClass().getSimpleName().equals( "ComponentDescriptorView" ) )
			{
				return input.toString().equals( "Web Page Runner" );
			}
			return false;
		}
	};

	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();
		controller = GUI.getController();
	}

	@Test
	public void canRunWebRunnerAndAbortExecution() throws Exception
	{

		ProjectItem project = ProjectLoadedWithoutAgentsState.STATE.getProject();

		controller.click( "#Generators" ).drag( FIXED_RATE ).by( 150, -50 ).drop();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestFX.findAll( ".canvas-object-view" ).size() == 1;
			}
		} );

		controller.click( "#Runners .expander-button" ).sleep( 500 ).drag( WEB_PAGE_RUNNER ).by( 150, 150 ).drop();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestFX.findAll( ".canvas-object-view" ).size() == 2;
			}
		}, 30 );

		Set<Node> outputs = TestFX.findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = TestFX.findAll( ".canvas-object-view .terminal-view.input-terminal" );

		System.out.println( "Outputs: " + outputs.size() );
		System.out.println( "Inputs: " + inputs.size() );

		controller.drag( Iterables.get( outputs, 0 ) ).to( Iterables.get( inputs, 1 ) )
				.click( ".canvas-object-view .text-input" ).type( "132.134.110.6" )
				.click( ".project-playback-panel .play-button" ).sleep( 2000 );

		assertTrue( project.isRunning() );

		controller.click( ".project-playback-panel .play-button" ).sleep( 500 );

		@SuppressWarnings( "unchecked" )
		List<Stage> stages = ( List<Stage> )TestFX.find( ".canvas-object-view" ).getScene().getRoot().getProperties()
				.get( "OTHER_STAGES" );
		assertEquals( 1, stages.size() );

		Node abortButton = stages.get( 0 ).getScene().lookup( "#abort-requests" );
		controller.click( abortButton ).sleep( 500 );

		assertFalse( project.isRunning() );

	}

}
