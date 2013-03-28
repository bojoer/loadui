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
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
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
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class CarouselTest
{
	private static Carousel<Rectangle> carousel;
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static final List<Rectangle> rectangles = new ArrayList<>();
	private static Stage stage;
	private static TestFX controller;

	public static class CarouselTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			carousel = new Carousel<>( "ToolBox" );
			addTestItems();

			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 300 ).height( 150 ).root( carousel ).build() );
			primaryStage.show();

			stageFuture.set( primaryStage );
		}

	}

	private static Rectangle buildRect( Color color )
	{
		Rectangle rectangle = RectangleBuilder.create().width( 50 ).height( 75 ).fill( color ).id( color.toString() )
				.build();
		rectangles.add( rectangle );

		return rectangle;
	}

	private static void addTestItems()
	{
		carousel.getItems().setAll( buildRect( Color.RED ), buildRect( Color.ORANGE ), buildRect( Color.YELLOW ),
				buildRect( Color.GREEN ), buildRect( Color.BLUE ), buildRect( Color.INDIGO ), buildRect( Color.VIOLET ) );
	}

	private static void setItems() throws Exception
	{
		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				rectangles.clear();
				carousel.getItems().clear();
				addTestItems();
			}
		}, 5 );

	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = wrap( new FXScreenController() );
		FXTestUtils.launchApp( CarouselTestApp.class );
		stage = targetWindow( stageFuture.get( 5, TimeUnit.SECONDS ) );
		FXTestUtils.bringToFront( stage );
	}

	@Before
	public void setup() throws Exception
	{
		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				if( carousel.getItems().size() > 0 )
					carousel.setSelected( carousel.getItems().get( 0 ) );
			}
		}, 5 );
		FXTestUtils.printGraph( carousel );
	}

	@Test
	public void shouldHandleLessThanFull() throws Exception
	{
		setItems();
		controller.move( ".carousel" );

		while( !carousel.getItems().isEmpty() )
		{
			FXTestUtils.invokeAndWait( new Runnable()
			{
				@Override
				public void run()
				{
					carousel.getItems().remove( 0 );
				}
			}, 5 );
			controller.scroll( 1 ).scroll( -1 ).sleep( 300 );
		}

		for( final Rectangle rectangle : rectangles )
		{
			FXTestUtils.invokeAndWait( new Runnable()
			{
				@Override
				public void run()
				{
					carousel.getItems().add( rectangle );
				}
			}, 5 );
			controller.scroll( 1 ).scroll( -1 ).sleep( 300 );
		}
	}

	@Test
	public void shouldChangeSelectionUsingDropdown() throws Exception
	{
		setItems();
		controller.click( ".combo-box" ).moveBy( 0, 70 ).click();
		assertThat( carousel.getSelected(), sameInstance( rectangles.get( 2 ) ) );
	}

	@Test
	public void shouldScrollUsingButtons() throws Exception
	{
		setItems();

		testScrolling( new Runnable()
		{
			@Override
			public void run()
			{
				controller.click( find( ".nav.left" ) ).sleep( 300 );
			}
		}, new Runnable()
		{

			@Override
			public void run()
			{
				controller.click( find( ".nav.right" ) ).sleep( 300 );
			}
		} );
	}

	@Test
	public void shouldScrollUsingMouseWheel() throws Exception
	{
		setItems();

		controller.move( ".carousel" );
		testScrolling( new Runnable()
		{
			@Override
			public void run()
			{
				controller.scroll( -1 ).sleep( 300 );
			}
		}, new Runnable()
		{
			@Override
			public void run()
			{
				controller.scroll( 1 ).sleep( 300 );
			}
		} );
	}

	private static void testScrolling( Runnable prev, Runnable next ) throws Exception
	{
		Button prevButton = find( ".nav.left" );
		Button nextButton = find( ".nav.right" );
		assertThat( prevButton.isDisabled(), is( true ) );
		assertThat( nextButton.isDisabled(), is( false ) );

		assertThat( carousel.getSelected(), sameInstance( rectangles.get( 0 ) ) );
		assertThat( rectangles.get( 0 ).getScene(), notNullValue() );
		assertThat( rectangles.get( 1 ).getScene(), notNullValue() );
		assertThat( rectangles.get( 2 ).getScene(), notNullValue() );
		assertThat( rectangles.get( 3 ).getScene(), nullValue() );

		next.run();
		assertThat( carousel.getSelected(), sameInstance( rectangles.get( 1 ) ) );
		assertThat( prevButton.isDisabled(), is( false ) );
		assertThat( rectangles.get( 3 ).getScene(), notNullValue() );

		next.run();
		next.run();
		next.run();
		assertThat( carousel.getSelected(), sameInstance( rectangles.get( 4 ) ) );
		assertThat( rectangles.get( 0 ).getScene(), nullValue() );
		assertThat( rectangles.get( 3 ).getScene(), notNullValue() );

		next.run();
		next.run();
		assertThat( carousel.getSelected(), sameInstance( rectangles.get( 6 ) ) );
		assertThat( nextButton.isDisabled(), is( true ) );

		prev.run();
		assertThat( carousel.getSelected(), sameInstance( rectangles.get( 5 ) ) );
		assertThat( nextButton.isDisabled(), is( false ) );
		assertThat( rectangles.get( 2 ).getScene(), nullValue() );
		assertThat( rectangles.get( 3 ).getScene(), notNullValue() );
	}

	@Test
	public void shouldChangeSelectionWhenAddingNewNodes() throws Exception
	{
		final Rectangle whiteRect = buildRect( Color.WHITE );

		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				carousel.getItems().clear();
			}
		}, 5 );

		assertThat( carousel.getSelected(), nullValue() );

		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				carousel.getItems().add( whiteRect );
			}
		}, 5 );

		assertThat( carousel.getSelected(), sameInstance( whiteRect ) );
		assertThat( carousel.getSelected().getStyleClass().contains( "selected" ), is( true ) );

	}

	@Test
	public void shouldDisplayCorrectNumberOfNodesWhenAddingNewNodes() throws Exception
	{
		final Rectangle aPinkRect = RectangleBuilder.create().fill( Color.PINK ).styleClass( "pink" ).build();
		final Rectangle bPurpleRect = RectangleBuilder.create().fill( Color.PURPLE ).styleClass( "purple" ).build();
		final Rectangle cRedRect = RectangleBuilder.create().fill( Color.RED ).styleClass( "red" ).build();

		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				carousel.getItems().clear();
			}
		}, 5 );

		assertThat( carousel.getSelected(), nullValue() );

		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				carousel.getItems().addAll( aPinkRect, bPurpleRect );
				carousel.getItems().addAll( cRedRect );
			}
		}, 5 );

		assertThat( carousel.getSelected(), notNullValue() );
		assertEquals( carousel.getItems().size(), 3 );
		controller.move( ".nav.left" );
		assertThat( carousel.getSelected(), sameInstance( cRedRect ) );
		controller.click( ".nav.left" ).sleep( 200 );
		assertThat( carousel.getSelected(), sameInstance( bPurpleRect ) );
		controller.click( ".nav.left" ).sleep( 200 );
		assertThat( carousel.getSelected(), sameInstance( aPinkRect ) );
	}

}
