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
package com.eviware.loadui.ui.fx.input;

import static com.eviware.loadui.ui.fx.util.test.TestFX.offset;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.PaneBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.api.input.Selectable;
import com.eviware.loadui.ui.fx.input.SelectableImpl;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.google.common.util.concurrent.SettableFuture;
import com.sun.javafx.PlatformUtil;

@Category( GUITest.class )
public class SelectableTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Selectable selectable1;
	private static Selectable selectable2;
	private static Stage stage;
	private static TestFX controller;
	private static Pane background;

	public static class SelectableTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			Rectangle rect1 = RectangleBuilder.create().id( "rect1" ).width( 25 ).height( 25 ).fill( Color.BLUE ).build();

			Rectangle rect2 = RectangleBuilder.create().id( "rect2" ).width( 50 ).height( 50 ).layoutX( 100 )
					.layoutY( 100 ).build();

			background = PaneBuilder.create().children( rect2, rect1 ).build();

			SelectableImpl.installDragToSelectArea( background );
			selectable1 = SelectableImpl.installSelectable( rect1 );
			selectable2 = SelectableImpl.installSelectable( rect2 );

			rect1.fillProperty().bind(
					Bindings.when( selectable1.selectedProperty() ).then( Color.GREEN ).otherwise( Color.GREY ) );

			rect2.fillProperty().bind(
					Bindings.when( selectable2.selectedProperty() ).then( Color.GREEN ).otherwise( Color.GREY ) );

			primaryStage.setScene( SceneBuilder.create().width( 300 ).height( 200 ).root( background ).build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = TestFX.wrap( new FXScreenController() );
		FXTestUtils.launchApp( SelectableTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		TestFX.targetWindow( stage );
		FXTestUtils.bringToFront( stage );
	}

	@After
	public void restorePosition()
	{
		selectable1.deselect();
		selectable2.deselect();
		controller.release( KeyCode.SHIFT ).release( KeyCode.CONTROL );
		FXTestUtils.awaitEvents();
	}

	@Test
	public void shouldHandlePrimaryMouseButtonClicks() throws Throwable
	{
		final Node rectangle1 = selectable1.getNode();
		final Node rectangle2 = selectable2.getNode();

		assertThat( selectable1.isSelected(), is( false ) );
		controller.click( rectangle1 );
		assertThat( selectable1.isSelected(), is( true ) );
		controller.click( background );
		assertThat( selectable1.isSelected(), is( false ) );
		controller.click( rectangle2 );
		assertThat( selectable2.isSelected(), is( true ) );
		controller.click( rectangle1 );
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( false ) );
	}

	@Test
	public void shouldHandleShiftKey() throws Throwable
	{
		final Node rectangle1 = selectable1.getNode();
		final Node rectangle2 = selectable2.getNode();

		controller.press( KeyCode.SHIFT ).click( rectangle1 ).click( rectangle2 );
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( true ) );
		controller.click( rectangle1 ).release( KeyCode.SHIFT );

		assertThat( selectable1.isSelected(), is( false ) );

		controller.press( KeyCode.SHIFT ).click( rectangle1 ).release( KeyCode.SHIFT ).click( rectangle2 );
		assertThat( selectable2.isSelected(), is( true ) );
		assertThat( selectable1.isSelected(), is( false ) );
	}

	@Test
	public void shouldHandleDragToSelect() throws Throwable
	{
		final Node rectangle1 = selectable1.getNode();
		final Node rectangle2 = selectable2.getNode();

		controller.drag( offset( background, 0, 0 ) ).to( rectangle2 );
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( true ) );
		controller.drag( rectangle1 ).by( 80, 80 ).drop();
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( false ) );
		controller.press( KeyCode.SHIFT ).drag( offset( background, 290, 10 ) ).to( offset( background, 0, 190 ) )
				.release( KeyCode.SHIFT );
		assertThat( selectable1.isSelected(), is( false ) );
		assertThat( selectable2.isSelected(), is( true ) );
		controller.click( background );
		assertThat( selectable2.isSelected(), is( false ) );
	}

	@Test
	public void shouldNotDragWhenShortcutKeyIsDown() throws Throwable
	{
		final Node rectangle1 = selectable1.getNode();
		final Node rectangle2 = selectable2.getNode();

		controller.drag( offset( background, 0, 0 ) ).to( rectangle2 );
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( true ) );

		if( PlatformUtil.isMac() )
			controller.press( KeyCode.META );
		else
			controller.press( KeyCode.CONTROL );

		controller.drag( rectangle1 ).by( 80, 80 ).drop();

		if( PlatformUtil.isMac() )
			controller.release( KeyCode.META );
		else
			controller.release( KeyCode.CONTROL );

		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( true ) );
	}
}
