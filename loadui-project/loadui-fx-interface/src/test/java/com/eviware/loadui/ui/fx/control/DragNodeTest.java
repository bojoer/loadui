package com.eviware.loadui.ui.fx.control;

import static org.junit.Assert.assertFalse;
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
public class DragNodeTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static DragNode dragNode;
	private static Stage stage;
	private static ControllerApi controller;

	public static class DragNodeTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			Rectangle dragRect = RectangleBuilder.create().id( "dragrect" ).width( 25 ).height( 25 ).fill( Color.BLUE )
					.build();

			dragNode = DragNode.install( dragRect, RectangleBuilder.create().id( "dragnode" ).width( 25 ).height( 25 )
					.fill( Color.GREEN ).build() );

			Rectangle dropRect = RectangleBuilder.create().id( "droprect" ).width( 50 ).height( 50 ).layoutX( 100 )
					.layoutY( 100 ).build();
			dropRect.fillProperty().bind(
					Bindings.when( dragNode.acceptableProperty() ).then( Color.GREEN ).otherwise( Color.RED ) );

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
		FXTestUtils.launchApp( DragNodeTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		ControllerApi.targetWindow( stage );
		FXTestUtils.bringToFront( stage );
	}

	@Test
	public void shouldDragAndRelease()
	{
		assertFalse( dragNode.isDragging() );

		MouseMotion dragging = controller.drag( dragNode.getDragSource() ).by( 200, 50 );

		assertTrue( dragNode.isDragging() );

		dragging.drop();

		assertFalse( dragNode.isDragging() );
	}

	@Test
	public void shouldAcceptAndDrop() throws InterruptedException
	{
		Node dropArea = stage.getScene().lookup( "#droprect" );
		final CountDownLatch dropLatch = new CountDownLatch( 1 );

		dropArea.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
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
					dropLatch.countDown();
				}

				event.consume();
			}
		} );

		assertFalse( dragNode.isAcceptable() );

		MouseMotion dragging = controller.drag( dragNode.getDragSource() ).via( dropArea );

		assertTrue( dragNode.isAcceptable() );

		dragging.drop();

		dropLatch.await( 2, TimeUnit.SECONDS );
	}
}
