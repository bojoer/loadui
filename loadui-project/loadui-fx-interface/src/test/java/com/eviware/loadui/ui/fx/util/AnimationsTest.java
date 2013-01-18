package com.eviware.loadui.ui.fx.util;

import static com.eviware.loadui.ui.fx.util.test.TestFX.targetWindow;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.SceneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.google.common.util.concurrent.SettableFuture;

public class AnimationsTest
{

	public static class AnimationsTestApp extends Application
	{

		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 300 ).height( 150 ).root( rootBox ).build() );
			primaryStage.show();
			stageFuture.set( primaryStage );
		}

	}

	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();

	private static Stage stage;
	private static VBox rootBox = new VBox();
	

	@BeforeClass
	public static void setup()
	{
		FXTestUtils.launchApp( AnimationsTestApp.class );
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
	public void testSlideDown() throws Exception
	{
		assertNotNull( stage );
		final Rectangle rectangle = RectangleBuilder.create().width( 50 ).height( 75 ).fill( Color.RED ).id( "rec" )
				.build();
		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				rootBox.getChildren().clear();
				rootBox.getChildren().add( rectangle );
			}
		}, 5 );
		Animations anime = new Animations( rectangle, false, Duration.millis( 100 ), Duration.millis( 1 ),
				Duration.millis( 1 ) );
		assertFalse( rectangle.isVisible() );

		anime.slideDown();
		Thread.sleep( 50 );

		// somewhere in the middle of the animation
		Bounds bounds = rectangle.getBoundsInParent();
		assertTrue( bounds.getMaxY() < rectangle.getHeight() );
		assertTrue( rectangle.isVisible() );

		Thread.sleep( 100 );

		// end of animation
		bounds = rectangle.getBoundsInLocal();
		assertEquals( 0, bounds.getMinY(), 0.1 );
		assertTrue( rectangle.isVisible() );

		assertEquals( 1, TestFX.findAll( "#rec" ).size() );
	}

	@Test
	public void testSlideUp() throws Exception
	{
		assertNotNull( stage );
		final Rectangle rectangle = RectangleBuilder.create().width( 50 ).height( 75 ).fill( Color.RED ).id( "rec" )
				.build();
		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				rootBox.getChildren().clear();
				rootBox.getChildren().add( rectangle );
			}
		}, 5 );
		Animations anime = new Animations( rectangle, true, Duration.millis( 1 ), Duration.millis( 100 ),
				Duration.millis( 1 ) );
		assertTrue( rectangle.isVisible() );

		anime.slideUp();
		Thread.sleep( 50 );

		// somewhere in the middle of the animation
		Bounds bounds = rectangle.getBoundsInParent();
		assertTrue( bounds.getMaxY() < rectangle.getHeight() );
		assertTrue( rectangle.isVisible() );

		Thread.sleep( 100 );

		// end of animation
		bounds = rectangle.getBoundsInLocal();
		assertEquals( 0, bounds.getMinY(), 0.1 );
		assertEquals( rectangle.getHeight(), bounds.getMaxY(), 0.1 );
		assertFalse( rectangle.isVisible() );

		assertEquals( 1, TestFX.findAll( "#rec" ).size() );
	}

	@Test
	public void testFadeAway() throws Exception
	{
		assertNotNull( stage );
		final Rectangle rectangle = RectangleBuilder.create().width( 50 ).height( 75 ).fill( Color.RED ).id( "rec" )
				.build();
		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				rootBox.getChildren().clear();
				rootBox.getChildren().add( rectangle );
			}
		}, 5 );
		Animations anime = new Animations( rectangle, true, Duration.millis( 1 ), Duration.millis( 1 ),
				Duration.millis( 100 ) );
		assertTrue( rectangle.isVisible() );
		double opacity = rectangle.getOpacity();
		assertEquals( 1.0, opacity, 0.01 );

		anime.fadeAway();
		Thread.sleep( 50 );

		// somewhere in the middle of the animation
		opacity = rectangle.getOpacity();
		assertTrue( 0.0 < opacity && opacity < 1.0 );
		assertTrue( rectangle.isVisible() );

		Thread.sleep( 100 );

		// end of animation
		opacity = rectangle.getOpacity();
		assertEquals( 1.0, opacity, 0.01 );
		assertFalse( rectangle.isVisible() );

		assertEquals( 1, TestFX.findAll( "#rec" ).size() );
	}

	@Test
	public void testAnimationIsCancelledWhenAnotherStarts() throws Exception
	{
		assertNotNull( stage );
		final Rectangle rectangle = RectangleBuilder.create().width( 50 ).height( 75 ).fill( Color.RED ).id( "rec" )
				.build();
		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				rootBox.getChildren().clear();
				rootBox.getChildren().add( rectangle );
			}
		}, 5 );
		Animations anime = new Animations( rectangle, true, Duration.millis( 100 ), Duration.millis( 100 ),
				Duration.millis( 100 ) );

		anime.fadeAway();
		Thread.sleep( 25 );

		// somewhere in the middle of the animation
		// this should cancel the fadeaway animation and starts the slideDown
		anime.slideDown();
		Thread.sleep( 25 );
		double opacity = rectangle.getOpacity();
		assertEquals( 1.0, opacity, 0.01 );
		Bounds bounds = rectangle.getBoundsInParent();
		assertTrue( bounds.getMaxY() < rectangle.getHeight() );
		assertTrue( rectangle.isVisible() );

		Thread.sleep( 75 );

		// end of animation
		opacity = rectangle.getOpacity();
		assertEquals( 1.0, opacity, 0.01 );
		bounds = rectangle.getBoundsInLocal();
		assertEquals( 0, bounds.getMinY(), 0.1 );
		assertTrue( rectangle.isVisible() );

	}

	@Test
	public void testSequenceOfAnimations() throws Exception
	{
		assertNotNull( stage );
		final Rectangle rectangle = RectangleBuilder.create().width( 50 ).height( 75 ).fill( Color.RED ).id( "rec" )
				.build();
		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				rootBox.getChildren().clear();
				rootBox.getChildren().add( rectangle );
			}
		}, 5 );
		Animations anime = new Animations( rectangle, true, Duration.millis( 50 ), Duration.millis( 50 ),
				Duration.millis( 50 ) );
		
		anime.fadeAway();
		Thread.sleep( 75 );
		assertFalse( rectangle.isVisible() );
		
		anime.slideDown();
		Thread.sleep( 25 );
		
		double opacity = rectangle.getOpacity();
		assertEquals( 1.0, opacity, 0.01 );
		Bounds bounds = rectangle.getBoundsInParent();
		assertTrue( bounds.getMaxY() < rectangle.getHeight() );
		assertTrue( rectangle.isVisible() );
		
		Thread.sleep( 50 );
		anime.slideUp();
		Thread.sleep( 75 );
		
		bounds = rectangle.getBoundsInLocal();
		assertEquals( 0, bounds.getMinY(), 0.1 );
		assertEquals( rectangle.getHeight(), bounds.getMaxY(), 0.1 );
		assertFalse( rectangle.isVisible() );

		

	}

}
