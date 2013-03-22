package com.eviware.loadui.ui.fx.util;

import static com.eviware.loadui.ui.fx.util.test.TestFX.targetWindow;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class NodeUtilsTest
{

	public static class NodeUtilsTestApp extends Application
	{

		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 300 ).height( 150 ).root( new VBox() ).build() );
			primaryStage.show();
			stageFuture.set( primaryStage );
		}

	}

	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static TestFX controller;
	private static Stage stage;

	@BeforeClass
	public static void setup()
	{
		controller = TestFX.wrap( new FXScreenController() );
		FXTestUtils.launchApp( NodeUtilsTestApp.class );
		try
		{
			stage = targetWindow( stageFuture.get( 5, TimeUnit.SECONDS ) );
			FXTestUtils.bringToFront( stage );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	@Test
	public void testAbsoluteBoundsOf() throws Exception
	{
		assertNotNull( stage );
		stage.setX( 50 );
		stage.setY( 100 );

		class Holder
		{
			Bounds bounds;
		}
		final Holder holder = new Holder();

		final SettableFuture<Boolean> future = SettableFuture.create();
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				StackPane pane = new StackPane();

				stage.getScene().setRoot( pane );

				Node node = new Rectangle( 50, 25, 10, 12 );
				pane.getChildren().add( node );

				holder.bounds = NodeUtils.absoluteBoundsOf( node );
				future.set( true );
			}
		} );

		assertTrue( future.get( 2, TimeUnit.SECONDS ) );
		Thread.sleep( 250 );

		assertEquals( 100D + stage.getScene().getX(), holder.bounds.getMinX(), 0.1 );
		assertEquals( 125D + stage.getScene().getY(), holder.bounds.getMinY(), 0.1 );
		assertEquals( 110D + stage.getScene().getX(), holder.bounds.getMaxX(), 0.1 );
		assertEquals( 137D + stage.getScene().getY(), holder.bounds.getMaxY(), 0.1 );

	}

	@Test
	public void testIsMouseOn() throws Exception
	{
		final Node node = new Rectangle( 5, 5 );
		final SettableFuture<Boolean> future = SettableFuture.create();
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				VBox box = new VBox();
				box.setPadding( new Insets( 55 ) );
				stage.getScene().setRoot( box );
				box.getChildren().add( node );
				future.set( true );
			}
		} );

		assertTrue( future.get( 2, TimeUnit.SECONDS ) );
		Thread.sleep( 250 );

		controller.move( node );
		assertTrue( NodeUtils.isMouseOn( node ) );

		controller.moveBy( 5, 0 );
		assertFalse( NodeUtils.isMouseOn( node ) );
	}

}
