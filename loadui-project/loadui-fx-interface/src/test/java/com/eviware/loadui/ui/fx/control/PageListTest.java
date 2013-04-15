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
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;
import javafx.util.Callback;

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
public class PageListTest
{
	private static PageList<Rectangle> pageList;
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static final List<Rectangle> rectangles = new ArrayList<>();
	private static Stage stage;
	private static TestFX controller;

	public static class PageListTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			pageList = new PageList<>( "PageList" );
			pageList.setLabelFactory( new Callback<Rectangle, Label>()
			{
				@Override
				public Label call( Rectangle rect )
				{
					return new Label( rect.getFill().toString(), RectangleBuilder.create().fill( rect.getFill() ).width( 16 )
							.height( 16 ).build() );
				}
			} );
			pageList.setWidthPerItem( 60 );
			pageList.getItems().setAll( buildRect( Color.RED ), buildRect( Color.ORANGE ), buildRect( Color.YELLOW ),
					buildRect( Color.GREEN ), buildRect( Color.BLUE ), buildRect( Color.INDIGO ), buildRect( Color.VIOLET ) );

			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 370 ).height( 150 ).root( pageList ).build() );
			primaryStage.show();

			stageFuture.set( primaryStage );
		}

		private static Rectangle buildRect( Color color )
		{
			Rectangle rectangle = RectangleBuilder.create().width( 50 ).height( 75 ).fill( color ).id( color.toString() )
					.build();
			rectangles.add( rectangle );

			return rectangle;
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = wrap( new FXScreenController() );
		FXTestUtils.launchApp( PageListTestApp.class );
		stage = targetWindow( stageFuture.get( 5, TimeUnit.SECONDS ) );
		FXTestUtils.bringToFront( stage );
	}

	@Before
	public void setup() throws Exception
	{
		controller.click( ".nav.left" ).click().click().click();
	}

	@Test
	public void shouldHandleLessThanFull() throws Exception
	{
		controller.move( ".page-list" );

		while( !pageList.getItems().isEmpty() )
		{
			FXTestUtils.invokeAndWait( new Runnable()
			{
				@Override
				public void run()
				{
					pageList.getItems().remove( 0 );
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
					pageList.getItems().add( rectangle );
				}
			}, 5 );
			controller.scroll( 1 ).scroll( -1 ).sleep( 300 );
		}
	}

	@Test
	public void shouldScrollUsingButtons() throws Exception
	{
		testScrolling( new Runnable()
		{
			@Override
			public void run()
			{
				controller.click( find( ".nav.prev" ) ).sleep( 300 );
			}
		}, new Runnable()
		{

			@Override
			public void run()
			{
				controller.click( find( ".nav.next" ) ).sleep( 300 );
			}
		} );
	}

	@Test
	public void shouldScrollUsingMouseWheel() throws Exception
	{
		controller.move( ".page-list" );
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
		Button prevButton = find( ".nav.prev" );
		Button nextButton = find( ".nav.next" );
		assertThat( prevButton.isDisabled(), is( true ) );
		assertThat( nextButton.isDisabled(), is( false ) );

		assertThat( rectangles.get( 0 ).getScene(), notNullValue() );
		assertThat( rectangles.get( 1 ).getScene(), notNullValue() );
		assertThat( rectangles.get( 2 ).getScene(), notNullValue() );
		assertThat( rectangles.get( 3 ).getScene(), notNullValue() );
		assertThat( rectangles.get( 4 ).getScene(), nullValue() );

		next.run();
		assertThat( prevButton.isDisabled(), is( false ) );
		assertThat( rectangles.get( 4 ).getScene(), notNullValue() );

		next.run();
		next.run();
		next.run();
		assertThat( rectangles.get( 0 ).getScene(), nullValue() );
		assertThat( rectangles.get( 4 ).getScene(), notNullValue() );

		next.run();
		next.run();
		assertThat( nextButton.isDisabled(), is( true ) );

		prev.run();
		assertThat( nextButton.isDisabled(), is( false ) );
		assertThat( rectangles.get( 1 ).getScene(), nullValue() );
		assertThat( rectangles.get( 2 ).getScene(), notNullValue() );
	}
}
