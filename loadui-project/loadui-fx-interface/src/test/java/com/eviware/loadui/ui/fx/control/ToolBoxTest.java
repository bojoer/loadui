package com.eviware.loadui.ui.fx.control;

import static com.eviware.loadui.ui.fx.util.test.ControllerApi.find;
import static com.eviware.loadui.ui.fx.util.test.ControllerApi.use;
import static com.eviware.loadui.ui.fx.util.test.ControllerApi.wrap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
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
import com.eviware.loadui.ui.fx.util.test.ControllerApi;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class ToolBoxTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static final Multimap<Color, Rectangle> rectangles = LinkedListMultimap.create();
	private static final List<Rectangle> clicked = new ArrayList<>();
	private static Stage stage;
	private static ControllerApi controller;

	public static class ToolboxTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			ToolBox<Rectangle> toolbox = new ToolBox<>( "ToolBox" );
			toolbox.getItems().setAll( buildRect( Color.RED ), buildRect( Color.RED ), buildRect( Color.BLUE ),
					buildRect( Color.GREEN ), buildRect( Color.RED ), buildRect( Color.YELLOW ), buildRect( Color.BLUE ),
					buildRect( Color.ORANGE ) );

			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 100 ).height( 350 ).root( toolbox ).build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}

		private static Rectangle buildRect( Color color )
		{
			final Rectangle rectangle = RectangleBuilder.create().width( 50 ).height( 75 ).fill( color ).build();
			ToolBox.setCategory( rectangle, color.toString() );

			rectangles.put( color, rectangle );
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

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = wrap( new FXScreenController() );
		FXTestUtils.launchApp( ToolboxTestApp.class );
		stage = use( stageFuture.get( 5, TimeUnit.SECONDS ) );
		FXTestUtils.bringToFront( stage );
	}

	@Before
	public void setup()
	{
		clicked.clear();
		use( stage );
		Button prevButton = find( ".nav.up" );
		controller.click( prevButton ).click( prevButton ).click( prevButton );
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

		controller.using( stage ).click( ".tool-box .title" );
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
				controller.click( find( ".nav.up" ) );
			}
		}, new Runnable()
		{

			@Override
			public void run()
			{
				controller.click( find( ".nav.down" ) );
			}
		} );
	}

	@Test
	public void shouldScrollUsingMouseWheel() throws Exception
	{
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
}
