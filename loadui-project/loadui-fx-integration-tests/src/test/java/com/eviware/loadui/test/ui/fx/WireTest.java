package com.eviware.loadui.test.ui.fx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.Callable;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@Category( IntegrationTest.class )
public class WireTest
{
	private static final Predicate<Node> CONDITION_COMPONENT = new Predicate<Node>()
	{
		@Override
		public boolean apply( Node input )
		{
			if( input.getClass().getSimpleName().equals( "ComponentDescriptorView" ) )
			{
				return input.toString().equals( "Condition" );
			}
			return false;
		}
	};

	private static TestFX controller;

	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		controller = GUI.getController();

		System.out.println( "Create Component 1" );
		controller.click( "#Flow.category .expander-button" ).drag( CONDITION_COMPONENT ).by( 100, -400 ).drop();
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

		System.out.println( "Create Component 2" );
		controller.click( "#Flow.category .expander-button" ).drag( CONDITION_COMPONENT ).by( 250, -100 ).drop();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestFX.findAll( ".canvas-object-view" ).size() == 2;
			}
		} );
	}

	@AfterClass
	public static void cleanState()
	{
		for( int components = 2; components > 0; components-- )
		{
			controller.click( ".component-view #menu" ).click( "#delete-item" ).click( ".confirmation-dialog #default" );
		}
	}

	@Test
	public void shouldDeleteSelectedWiresByClickingOnDelete()
	{
		Set<Node> outputs = TestFX.findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = TestFX.findAll( ".canvas-object-view .terminal-view.input-terminal" );

		controller.drag( Iterables.get( inputs, 0 ) ).to( Iterables.get( outputs, 2 ) );

		controller.move( Iterables.get( inputs, 0 ) ).moveBy( 0, -25 ).click();
		assertThat( TestFX.findAll( ".connection-view" ).size(), is( 1 ) );

		controller.type( KeyCode.DELETE );
		assertThat( TestFX.findAll( ".connection-view" ).size(), is( 0 ) );
	}

	@Test
	@Ignore( "LOADUI-64 - Skipped this for release 2.5 as implementation would be too time-consuming" )
	public void shouldDeleteSelectedWiresByRightClickMenu()
	{
		Set<Node> outputs = TestFX.findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = TestFX.findAll( ".canvas-object-view .terminal-view.input-terminal" );

		controller.drag( Iterables.get( inputs, 0 ) ).to( Iterables.get( outputs, 2 ) );

		assertThat( TestFX.findAll( ".connection-view" ).size(), is( 1 ) );
		
		controller.move( Iterables.get( inputs, 0 ) ).moveBy( 0, -25 ).click( MouseButton.SECONDARY )
				.click( "#delete-wire" );
		
		assertThat( TestFX.findAll( ".connection-view" ).size(), is( 0 ) );
	}

	@Test
	public void shouldCreateWires()
	{
		Set<Node> outputs = TestFX.findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = TestFX.findAll( ".canvas-object-view .terminal-view.input-terminal" );

		controller.drag( Iterables.get( outputs, 0 ) ).to( Iterables.get( inputs, 1 ) );
		controller.drag( Iterables.get( inputs, 0 ) ).to( Iterables.get( outputs, 2 ) );

		assertThat( TestFX.findAll( ".connection-view" ).size(), is( 2 ) );
		controller.drag( Iterables.get( outputs, 0 ) ).to( Iterables.get( outputs, 1 ) );
		assertThat( TestFX.findAll( ".connection-view" ).size(), is( 2 ) );

		Node terminal = Iterables.get( inputs, 0 );
		controller.move( terminal ).moveBy( 0, -25 ).click().drag( terminal ).by( 0, -30 ).drop();
		assertThat( TestFX.findAll( ".connection-view" ).size(), is( 1 ) );

		controller.drag( Iterables.get( outputs, 1 ) ).by( 0, 20 ).drop();
		assertThat( TestFX.findAll( ".connection-view" ).size(), is( 0 ) );
	}

	@Test
	public void preventMultipleConnectionsToASingleOutputTerminal()
	{
		Set<Node> outputs = TestFX.findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = TestFX.findAll( ".canvas-object-view .terminal-view.input-terminal" );

		controller.drag( Iterables.get( inputs, 0 ) ).to( Iterables.get( outputs, 0 ) );
		controller.drag( Iterables.get( inputs, 1 ) ).to( Iterables.get( outputs, 0 ) );

		assertThat( TestFX.findAll( ".connection-view" ).size(), is( 1 ) );

		controller.drag( Iterables.get( outputs, 0 ) ).by( 0, 20 ).drop();
		assertThat( TestFX.findAll( ".connection-view" ).size(), is( 0 ) );
	}
}
