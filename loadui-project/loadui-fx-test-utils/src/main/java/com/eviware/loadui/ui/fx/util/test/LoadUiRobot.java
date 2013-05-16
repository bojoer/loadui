package com.eviware.loadui.ui.fx.util.test;

import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;

import java.awt.Point;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import javafx.scene.Node;
import javafx.stage.Window;
import javafx.util.Duration;

import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class LoadUiRobot
{
	public enum Component
	{
		FIXED_RATE_GENERATOR( "Generators", "Fixed Rate" ), TABLE_LOG( "Output", "Table Log" ), WEB_PAGE_RUNNER(
				"Runners", "Web Page Runner" );

		public final String category;
		public final String name;

		private Component( String category, String name )
		{
			this.category = category;
			this.name = name;
		}
	}

	private Queue<Point> predefinedPoints = Lists.newLinkedList( ImmutableList.of( new Point( 250, 250 ), new Point(
			450, 450 ) ) );
	private TestFX controller;

	private LoadUiRobot( TestFX controller )
	{
		this.controller = controller;
	}

	public static LoadUiRobot usingController( TestFX controller )
	{
		return new LoadUiRobot( controller );
	}

	public ComponentHandle createComponent( final Component component ) throws Exception
	{
		Preconditions.checkNotNull( predefinedPoints.peek(),
				"All predefined points (x,y) for component placement are used. Please add new ones." );
		return createComponentAt( component, predefinedPoints.poll() );
	}

	public ComponentHandle createComponentAt( final Component component, Point targetPoint ) throws Exception
	{
		final int numberOfComponents = TestFX.findAll( ".canvas-object-view" ).size();
		Set<Node> oldOutputs = findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> oldInputs = findAll( ".canvas-object-view .terminal-view.input-terminal" );

		final Predicate<Node> isDescriptor = new Predicate<Node>()
		{
			@Override
			public boolean apply( Node input )
			{
				if( input.getClass().getSimpleName().equals( "ComponentDescriptorView" ) )
				{
					return input.toString().equals( component.name );
				}
				return false;
			}
		};

		int maxToolboxCategories = 50;
		while( TestFX.findAll( "#" + component.category + ".category" ).isEmpty() )
		{
			if( --maxToolboxCategories < 0 )
				throw new RuntimeException( "Could not find component category " + component.category
						+ " in component ToolBox." );
			controller.move( "#Runners.category" ).scroll( 10 );
		}

		if( !TestFX.findAll( "#" + component.category + ".category .expander-button" ).isEmpty() )
		{
			controller.click( "#" + component.category + ".category .expander-button" );
		}

		Window window = TestFX.find( "#Runners.category" ).getScene().getWindow();
		int windowX = ( int )window.getX();
		int windowY = ( int )window.getY();
		controller.drag( isDescriptor ).to( windowX + targetPoint.x, windowY + targetPoint.y );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestFX.findAll( ".canvas-object-view" ).size() == numberOfComponents + 1;
			}
		}, 25000 );

		Set<Node> outputs = findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = findAll( ".canvas-object-view .terminal-view.input-terminal" );
		inputs.removeAll( oldInputs );
		outputs.removeAll( oldOutputs );

		return new ComponentHandle( inputs, outputs, controller );
	}

	public void clickPlayStopButton()
	{
		controller.click( ".project-playback-panel .play-button" );
	}

	public void runTestFor( Duration duration )
	{
		clickPlayStopButton();
		controller.sleep( ( long )duration.toMillis() );
		clickPlayStopButton();
		controller.sleep( 2000 );
	}
}
