package com.eviware.loadui.ui.fx.control;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
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
import com.eviware.loadui.ui.fx.util.test.ControllerApi;
import com.eviware.loadui.ui.fx.util.test.ControllerApi.MouseMotion;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class MovableTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Movable movable;
	private static Stage stage;
	private static ControllerApi controller;

	public static class MovableTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			Rectangle dragRect = RectangleBuilder.create().id( "dragrect" ).width( 25 ).height( 25 ).fill( Color.BLUE )
					.build();
			movable = Movable.install( dragRect );

			Rectangle dropRect = RectangleBuilder.create().id( "droprect" ).width( 50 ).height( 50 ).layoutX( 100 )
					.layoutY( 100 ).build();
			dropRect.fillProperty().bind(
					Bindings.when( movable.acceptableProperty() ).then( Color.GREEN ).otherwise( Color.RED ) );

			primaryStage.setScene( SceneBuilder.create().width( 300 ).height( 200 )
					.root( GroupBuilder.create().children( dropRect, dragRect ).build() ).build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = ControllerApi.wrap( new FXScreenController() );
		FXTestUtils.launchApp( MovableTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		ControllerApi.targetWindow( stage );
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
