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
package com.eviware.loadui.test.ui.fx.states;

import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;

import java.util.Set;
import java.util.concurrent.Callable;

import javafx.scene.Node;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class SimpleWebTestState extends TestState
{
	private static final Predicate<Node> WEB_RUNNER = new Predicate<Node>()
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

	public static final SimpleWebTestState STATE = new SimpleWebTestState();

	private SimpleWebTestState()
	{
		this( "Simple Web Test", ProjectLoadedWithoutAgentsState.STATE );
	}

	protected SimpleWebTestState( String name, TestState parent )
	{
		super( name, parent );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		log.debug( "Creating simple web test." );
		TestFX controller = GUI.getController();

		controller.click( "#Runners.category .expander-button" ).drag( WEB_RUNNER ).by( 100, 150 ).drop();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestFX.findAll( ".canvas-object-view" ).size() == 1;
			}
		}, 25000 );
		controller.click( ".component-view .text-field" ).type( "www.google.com" );

		controller.click( "#Generators.category .expander-button" ).drag( FIXED_RATE ).by( 200, -150 ).drop();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestFX.findAll( ".canvas-object-view" ).size() == 2;
			}
		} );

		Set<Node> outputs = TestFX.findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = TestFX.findAll( ".canvas-object-view .terminal-view.input-terminal" );

		controller.drag( Iterables.get( outputs, 2 ) ).to( Iterables.get( inputs, 0 ) );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		GUI.getController().click( "#closeProjectButton" );
		//If there is a save dialog, do not save:
		try
		{
			GUI.getController().click( "#no" ).target( TestFX.getWindowByIndex( 0 ) );
		}
		catch( Exception e )
		{
			//Ignore
		}

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll( ".workspace-view" ).isEmpty();
			}
		} );
	}
}
