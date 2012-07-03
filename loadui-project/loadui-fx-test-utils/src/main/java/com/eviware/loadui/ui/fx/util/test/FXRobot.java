package com.eviware.loadui.ui.fx.util.test;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import com.eviware.loadui.util.test.TestUtils;
import com.google.common.collect.ImmutableMap;

@Deprecated
public class FXRobot
{
	private static final Map<MouseButton, Integer> BUTTONS = ImmutableMap.of( MouseButton.PRIMARY,
			InputEvent.BUTTON1_MASK, MouseButton.MIDDLE, InputEvent.BUTTON2_MASK, MouseButton.SECONDARY,
			InputEvent.BUTTON3_MASK );
	private final Robot robot;

	private final DoubleProperty mouseXProperty = new SimpleDoubleProperty();

	public DoubleProperty mouseXProperty()
	{
		return mouseXProperty;
	}

	public double getMouseX()
	{
		return mouseXProperty.get();
	}

	public void setMouseX( double mouseX )
	{
		if( stage != null )
		{
			mouseX += stage.getX() + stage.getScene().getX();
		}
		mouseXProperty.set( mouseX );
	}

	private final DoubleProperty mouseYProperty = new SimpleDoubleProperty();

	public DoubleProperty mouseYProperty()
	{
		return mouseYProperty;
	}

	public double getMouseY()
	{
		return mouseYProperty.get();
	}

	public void setMouseY( double mouseY )
	{
		if( stage != null )
		{
			mouseY += stage.getY() + stage.getScene().getY();
		}
		mouseYProperty.set( mouseY );
	}

	private Stage stage;

	public void setStage( Stage stage )
	{
		this.stage = stage;
	}

	private long moveTime = 500;

	public void setMoveTime( long moveTime )
	{
		this.moveTime = moveTime;
	}

	public FXRobot() throws AWTException
	{
		robot = new Robot();

		final ChangeListener<Number> mouseChangeListener = new ChangeListener<Number>()
		{
			@Override
			public void changed( ObservableValue<? extends Number> value, Number oldNum, Number newNum )
			{
				robot.mouseMove( mouseXProperty.intValue(), mouseYProperty.intValue() );
			}
		};

		mouseXProperty.addListener( mouseChangeListener );
		mouseYProperty.addListener( mouseChangeListener );
	}

	public void mouseMove( Point2D point, long time )
	{
		mouseMove( point.getX(), point.getY(), time );
	}

	public void mouseMove( double x, double y, final long time )
	{
		final CountDownLatch done = new CountDownLatch( 1 );

		final double absoluteX = stage != null ? stage.getX() + stage.getScene().getX() + x : x;
		final double absoluteY = stage != null ? stage.getY() + stage.getScene().getY() + y : y;

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				new Timeline( new KeyFrame( new Duration( time ), new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						done.countDown();
					}
				}, new KeyValue( mouseXProperty, absoluteX, Interpolator.EASE_BOTH ), new KeyValue( mouseYProperty,
						absoluteY, Interpolator.EASE_BOTH ) ) ).playFromStart();
			}
		} );

		try
		{
			done.await();
			FXTestUtils.awaitEvents();
		}
		catch( InterruptedException e )
		{
			throw new RuntimeException( e );
		}
	}

	public void mouseMove( Point2D point )
	{
		mouseMove( point.getX(), point.getY() );
	}

	public void mouseMove( double x, double y )
	{
		setMouseX( x );
		setMouseY( y );
		FXTestUtils.awaitEvents();
	}

	public void mouseMoveBy( double x, double y )
	{
		Stage oldStage = stage;
		try
		{
			stage = null;
			mouseMove( getMouseX() + x, getMouseY() + y, moveTime );
		}
		finally
		{
			stage = oldStage;
		}
	}

	public void mousePress( MouseButton button )
	{
		if( button == null )
		{
			return;
		}
		robot.mousePress( BUTTONS.get( button ) );
		FXTestUtils.awaitEvents();
	}

	public void mouseRelease( MouseButton button )
	{
		if( button == null )
		{
			return;
		}
		robot.mouseRelease( BUTTONS.get( button ) );
		FXTestUtils.awaitEvents();
	}

	public MouseMotion move( double x, double y )
	{
		return drag( x, y, null );
	}

	public MouseMotion move( Point2D point )
	{
		return drag( point, null );
	}

	public MouseMotion move( Node node )
	{
		return drag( node, null );
	}

	public void click( MouseButton button )
	{
		mousePress( button );
		mouseRelease( button );
	}

	public void click()
	{
		click( MouseButton.PRIMARY );
	}

	public void click( double x, double y, MouseButton button )
	{
		mouseMove( x, y, moveTime );
		click( button );
	}

	public void click( Point2D point, MouseButton button )
	{
		click( point.getX(), point.getY(), button );
	}

	public void click( Node node, MouseButton button )
	{
		click( pointForNode( node ), button );
	}

	public void click( double x, double y )
	{
		click( x, y, MouseButton.PRIMARY );
	}

	public void click( Point2D point )
	{
		click( point, MouseButton.PRIMARY );
	}

	public void click( Node node )
	{
		click( node, MouseButton.PRIMARY );
	}

	public MouseMotion drag( double x, double y, MouseButton button )
	{
		return new MouseMotion( x, y, button );
	}

	public MouseMotion drag( Point2D point, MouseButton button )
	{
		return drag( point.getX(), point.getY(), button );
	}

	public MouseMotion drag( Node node, MouseButton button )
	{
		return drag( pointForNode( node ), button );
	}

	public MouseMotion drag( double x, double y )
	{
		return new MouseMotion( x, y, MouseButton.PRIMARY );
	}

	public MouseMotion drag( Point2D point )
	{
		return drag( point, MouseButton.PRIMARY );
	}

	public MouseMotion drag( Node node )
	{
		return drag( node, MouseButton.PRIMARY );
	}

	private Point2D pointForNode( final Node node )
	{
		//Sometimes the node is given before it has been laid out properly, so let's try to wait for it to have non-zero bounds:
		try
		{
			TestUtils.awaitCondition( new Callable<Boolean>()
			{
				@Override
				public Boolean call() throws Exception
				{
					return node.getLayoutBounds().getWidth() != 0.0 && node.getLayoutBounds().getHeight() != 0.0;
				}
			}, 1 );
		}
		catch( Exception e )
		{
		}

		Bounds bounds = node.getLayoutBounds();
		Point2D scenePoint = node.localToScene( ( bounds.getMinX() + bounds.getMaxX() / 2 ),
				( bounds.getMinY() + bounds.getMaxY() / 2 ) );
		Scene scene = node.getScene();
		Window window = scene.getWindow();
		if( window instanceof Stage )
		{
			stage = ( Stage )window;
			stage.toFront();

			return scenePoint;
		}
		else
		{
			return new Point2D( window.getX() + scene.getX() + scenePoint.getX(), window.getY() + scene.getY()
					+ scenePoint.getY() );
		}
	}

	public class MouseMotion
	{
		private MouseButton button;

		private MouseMotion( double x, double y, MouseButton button )
		{
			this.button = button;
			mouseMove( x, y, moveTime );
			mousePress( button );
		}

		public MouseMotion via( double x, double y )
		{
			mouseMove( x, y, moveTime );
			return this;
		}

		public MouseMotion via( Point2D point )
		{
			via( point.getX(), point.getY() );
			return this;
		}

		public MouseMotion via( Node node )
		{
			via( pointForNode( node ) );
			return this;
		}

		public MouseMotion by( double x, double y )
		{
			mouseMoveBy( x, y );
			return this;
		}

		public void to( double x, double y )
		{
			mouseMove( x, y, moveTime );
			mouseRelease( button );
		}

		public void to( Point2D point )
		{
			to( point.getX(), point.getY() );
		}

		public void to( Node node )
		{
			to( pointForNode( node ) );
		}

		public void drop()
		{
			mouseRelease( button );
		}
	}
}
