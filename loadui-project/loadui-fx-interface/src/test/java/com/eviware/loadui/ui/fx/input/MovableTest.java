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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.ui.fx.util.test.TestFX.MouseMotion;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class MovableTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static MovableImpl movable;
	private static Stage stage;
	private static TestFX controller;
	private static Group group;

	public static class MovableTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			Rectangle dragRect = RectangleBuilder.create().id( "dragrect" ).width( 25 ).height( 25 ).fill( Color.BLUE )
					.build();
			movable = MovableImpl.install( dragRect );

			Rectangle dropRect = RectangleBuilder.create().id( "droprect" ).width( 50 ).height( 50 ).layoutX( 100 )
					.layoutY( 100 ).build();
			dropRect.fillProperty().bind(
					Bindings.when( movable.acceptableProperty() ).then( Color.GREEN ).otherwise( Color.RED ) );

			group = GroupBuilder.create().children( dropRect, dragRect ).build();

			primaryStage.setScene( SceneBuilder.create().width( 300 ).height( 200 ).root( group ).build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = TestFX.wrap( new FXScreenController() );
		FXTestUtils.launchApp( MovableTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		TestFX.targetWindow( stage );
		FXTestUtils.bringToFront( stage );
	}

	@After
	public void restorePosition()
	{
		final Node node = movable.getNode();
		node.setLayoutX( 0 );
		node.setLayoutY( 0 );
		FXTestUtils.awaitEvents();
	}

	@Test
	public void shouldMove() throws Throwable
	{
		final Node movableNode = movable.getNode();

		assertThat( movable.isDragging(), is( false ) );

		MouseMotion dragging = controller.drag( movableNode ).by( 100, 50 );

		assertThat( movable.isDragging(), is( true ) );

		dragging.drop();

		assertThat( movable.isDragging(), is( false ) );

		assertEquals( 100.0, movableNode.getLayoutX(), 1.0 );
		assertEquals( 50.0, movableNode.getLayoutY(), 1.0 );
	}

	@Test
	public void oldNodesShouldMove_after_newNodesHaveBeenAdded() throws Throwable
	{
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				group.getChildren().add(
						RectangleBuilder.create().id( "newrect" ).width( 15 ).height( 15 ).layoutX( 40 ).fill( Color.GRAY )
								.build() );
			}
		} );
		FXTestUtils.awaitEvents();

		shouldMove();
	}

	@Test
	public void shouldAcceptOnHover() throws Throwable
	{
		final Node movableNode = movable.getNode();
		final Node dropzone = stage.getScene().lookup( "#droprect" );

		dropzone.addEventHandler( DraggableEvent.DRAGGABLE_ENTERED, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				event.accept();
				event.consume();
			}
		} );

		assertThat( movable.isAcceptable(), is( false ) );

		MouseMotion dragging = controller.drag( movableNode ).via( dropzone );

		assertTrue( movable.isAcceptable() );

		dragging.by( -100, -50 );

		assertFalse( movable.isAcceptable() );

		dragging.drop();
	}

	@Test
	public void shouldDrop() throws Throwable
	{
		final Node movableNode = movable.getNode();
		final Node dropzone = stage.getScene().lookup( "#droprect" );

		final CountDownLatch droppedLatch = new CountDownLatch( 1 );

		dropzone.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
				{
					event.accept();
				}
				else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
				{
					droppedLatch.countDown();
				}
				event.consume();
			}
		} );

		controller.drag( movableNode ).to( dropzone );

		droppedLatch.await( 2, TimeUnit.SECONDS );
	}
}
