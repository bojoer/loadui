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
package com.eviware.loadui.ui.fx.control;

import static com.eviware.loadui.ui.fx.util.test.TestFX.find;
import static com.eviware.loadui.ui.fx.util.test.TestFX.targetWindow;
import static com.eviware.loadui.ui.fx.util.test.TestFX.wrap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class ToolBoxTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static final Multimap<Color, Rectangle> rectangles = LinkedListMultimap.create();
	private static final List<Rectangle> clicked = new ArrayList<>();
	private static Stage stage;
	private static TestFX controller;
	static ToolBox<Rectangle> toolbox;
	static final List<Rectangle> allRects = Arrays.asList( buildRect( Color.RED ), buildRect( Color.RED ),
			buildRect( Color.BLUE ), buildRect( Color.GREEN ), buildRect( Color.RED ), buildRect( Color.YELLOW ),
			buildRect( Color.BLUE ), buildRect( Color.ORANGE ) );
	static final List<Rectangle> rectsToAdd = Arrays.asList( RectangleBuilder.create().fill( Color.AQUA ).build() );

	public static class ToolboxTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			toolbox = new ToolBox<>( "ToolBox" );
			List<Rectangle> everything = new ArrayList<Rectangle>( allRects );
			everything.addAll( rectsToAdd );
			toolbox.setComparator( Ordering.explicit( everything ) );
			toolbox.setCategoryComparator( Ordering.explicit( Color.RED.toString(), Color.BLUE.toString(),
					Color.GREEN.toString(), Color.YELLOW.toString(), Color.ORANGE.toString(), "Renamed" ) );
			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 100 ).height( 350 ).root( toolbox ).build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}

	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = wrap( new FXScreenController() );
		FXTestUtils.launchApp( ToolboxTestApp.class );
		stage = targetWindow( stageFuture.get( 5, TimeUnit.SECONDS ) );
		FXTestUtils.bringToFront( stage );
	}

	@Before
	public void setup() throws Exception
	{
		clicked.clear();
		rectangles.clear();
		for( Rectangle r : allRects )
		{
			rectangles.put( ( Color )r.getFill(), r );
		}

		final SettableFuture<Boolean> future = SettableFuture.create();
		Platform.runLater( new Runnable()
		{

			@Override
			public void run()
			{
				toolbox.getItems().clear();
				toolbox.getItems().setAll( allRects );
				future.set( true );
			}
		} );

		targetWindow( stage );
		future.get( 5, TimeUnit.SECONDS );
	}

	@Test
	public void shouldExpandToShowCategory() throws Exception
	{
		Rectangle rectangle0 = Iterables.get( rectangles.get( Color.RED ), 0 );
		Rectangle rectangle1 = Iterables.get( rectangles.get( Color.RED ), 1 );
		Rectangle rectangle2 = Iterables.get( rectangles.get( Color.RED ), 2 );

		controller.click( ".expander-button" ).click( rectangle2 ).click( rectangle0 ).click( rectangle1 );

		assertThat( clicked, is( Arrays.asList( rectangle2, rectangle0, rectangle1 ) ) );
		PopupControl expander = ( PopupControl )rectangle2.getScene().getWindow();
		assertThat( expander.isShowing(), is( true ) );

		controller.target( stage ).click( ".tool-box .title" );
		assertThat( expander.isShowing(), is( false ) );
	}

	@Test
	public void shouldScrollUsingButtons() throws Exception
	{
		testScrolling( new Runnable()
		{
			@Override
			public void run()
			{
				controller.click( ".nav.up" );
			}
		}, new Runnable()
		{
			@Override
			public void run()
			{
				controller.click( ".nav.down" );
			}
		} );
	}

	@Test
	public void shouldScrollUsingMouseWheel() throws Exception
	{
		Button prevButton = find( ".nav.up" );
		controller.click( prevButton ).click( prevButton ).click( prevButton );
		testScrolling( new Runnable()
		{
			@Override
			public void run()
			{
				controller.move( ".tool-box" ).scroll( -1 );
			}
		}, new Runnable()
		{
			@Override
			public void run()
			{
				controller.move( ".tool-box" ).scroll( 1 );
			}
		} );
	}

	private static void testScrolling( Runnable prev, Runnable next ) throws Exception
	{
		Rectangle rectangle0 = Iterables.get( rectangles.get( Color.RED ), 0 );
		Rectangle rectangle1 = Iterables.get( rectangles.get( Color.BLUE ), 0 );
		Rectangle rectangle2 = Iterables.get( rectangles.get( Color.GREEN ), 0 );
		Rectangle rectangle3 = Iterables.get( rectangles.get( Color.YELLOW ), 0 );
		Rectangle rectangle4 = Iterables.get( rectangles.get( Color.ORANGE ), 0 );

		Button prevButton = find( ".nav.up" );
		Button nextButton = find( ".nav.down" );
		assertThat( prevButton.isDisabled(), is( true ) );
		assertThat( nextButton.isDisabled(), is( false ) );

		assertThat( rectangle0.getScene(), notNullValue() );
		assertThat( rectangle1.getScene(), notNullValue() );
		assertThat( rectangle2.getScene(), nullValue() );

		next.run();
		assertThat( prevButton.isDisabled(), is( false ) );
		assertThat( rectangle0.getScene(), nullValue() );
		assertThat( rectangle2.getScene(), notNullValue() );

		next.run();
		next.run();
		assertThat( rectangle3.getScene(), notNullValue() );
		assertThat( rectangle4.getScene(), notNullValue() );
		assertThat( nextButton.isDisabled(), is( true ) );

		prev.run();
		assertThat( nextButton.isDisabled(), is( false ) );
		assertThat( rectangle2.getScene(), notNullValue() );
		assertThat( rectangle4.getScene(), nullValue() );
	}

	@Test
	public void shouldChangeWhenAddingItemsAtRuntime() throws Exception
	{
		final Rectangle red = Iterables.get( rectangles.get( Color.RED ), 0 );
		final Rectangle blue = Iterables.get( rectangles.get( Color.BLUE ), 0 );
		final Rectangle green = Iterables.get( rectangles.get( Color.GREEN ), 0 );

		class Results
		{
			final SettableFuture<Object> clearTest = SettableFuture.create();
			final SettableFuture<Object> addTwoItemsTest = SettableFuture.create();
			final SettableFuture<Object> addItemTest = SettableFuture.create();
			final SettableFuture<Object> afterScrollingTest = SettableFuture.create();
		}
		final Results results = new Results();

		runLaterSettingRectangles( new Runnable()
		{
			@Override
			public void run()
			{
				toolbox.getItems().clear();
			}
		}, results.clearTest );
		runLaterSettingRectangles( new Runnable()
		{
			@Override
			public void run()
			{
				toolbox.getItems().add( red );
				toolbox.getItems().add( blue );
			}
		}, results.addTwoItemsTest );
		runLaterSettingRectangles( new Runnable()
		{
			@Override
			public void run()
			{
				toolbox.getItems().add( green );
			}
		}, results.addItemTest );

		Object clearTest = results.clearTest.get( 2, TimeUnit.SECONDS );
		Object addTwoItemsTest = results.addTwoItemsTest.get( 2, TimeUnit.SECONDS );
		Object addItemTest = results.addItemTest.get( 2, TimeUnit.SECONDS );

		assertFalse( clearTest instanceof Exception );
		assertFalse( addTwoItemsTest instanceof Exception );
		assertFalse( addItemTest instanceof Exception );

		assertTrue( ( ( Set<?> )clearTest ).isEmpty() );
		assertTrue( ( ( Set<?> )addTwoItemsTest ).containsAll( Arrays.asList( red, blue ) ) );
		assertTrue( ( ( Set<?> )addItemTest ).containsAll( Arrays.asList( red, blue ) ) ); // no change until clicking scroll button

		controller.click( ".nav.down" );

		runLaterSettingRectangles( new Runnable()
		{
			@Override
			public void run()
			{

			}
		}, results.afterScrollingTest );

		Object afterScrollingTest = results.afterScrollingTest.get( 1, TimeUnit.SECONDS );

		assertFalse( afterScrollingTest instanceof Exception );
		assertTrue( ( ( Set<?> )afterScrollingTest ).containsAll( Arrays.asList( blue, green ) ) );

	}

	@Test
	public void shouldChangeWhenRemovingItemsAtRuntime() throws Exception
	{
		final Rectangle orange = Iterables.get( rectangles.get( Color.ORANGE ), 0 );

		Button nextButton = find( ".nav.down" );
		for( int i = 0; i < 5; i++ )
			controller.click( nextButton );

		assertThat( orange.getScene(), notNullValue() );

		final SettableFuture<Boolean> future = SettableFuture.create();

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				toolbox.getItems().remove( orange );
				future.set( true );
			}
		} );

		assertTrue( future.get( 1, TimeUnit.SECONDS ) );
		assertThat( orange.getScene(), nullValue() );

	}

	@Test
	public void shouldChangeWhenRenamingCategory() throws Exception
	{
		final Rectangle orange = Iterables.get( rectangles.get( Color.ORANGE ), 0 );

		Button nextButton = find( ".nav.down" );
		for( int i = 0; i < 5; i++ )
			controller.click( nextButton );

		assertThat( orange.getScene(), notNullValue() );

		final SettableFuture<Boolean> future = SettableFuture.create();

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				ToolBox.setCategory( orange, "Renamed" );
				future.set( true );
			}
		} );

		assertTrue( future.get( 1, TimeUnit.SECONDS ) );
		controller.sleep( 500 ).click( nextButton );

		boolean foundRenamed = false;
		for( Node holder : TestFX.findAll( "Label" ) )
		{
			if( ( ( Label )holder ).getText().equals( "Renamed" ) )
			{
				foundRenamed = true;
				break;
			}
		}

		assertTrue( foundRenamed );

	}

	private void runLaterSettingRectangles( final Runnable runnable, final SettableFuture<Object> future )
			throws Exception
	{
		final SettableFuture<Boolean> runnableDone = SettableFuture.create();
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					runnable.run();
					System.out.println( "Runnable done" );
					runnableDone.set( true );
				}
				catch( Exception e )
				{
					future.set( e );
				}
			}
		} );

		runnableDone.get( 5, TimeUnit.SECONDS );
		Thread.sleep( 500 ); // really necessary, JavaFX seems to delay to update the graphics sometimes!!
		future.set( TestFX.findAll( "Rectangle" ) );
		System.out.println( "Set the future rectangles" );

	}

	private static Rectangle buildRect( Color color )
	{
		final Rectangle rectangle = RectangleBuilder.create().width( 50 ).height( 75 ).fill( color ).build();
		ToolBox.setCategory( rectangle, color.toString() );

		rectangle.addEventHandler( MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				clicked.add( rectangle );
			}
		} );

		return rectangle;
	}

}
