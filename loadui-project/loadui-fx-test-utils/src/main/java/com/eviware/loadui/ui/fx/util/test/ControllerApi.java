package com.eviware.loadui.ui.fx.util.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.Window;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class ControllerApi
{
	public static ControllerApi wrap( ScreenController controller )
	{
		return new ControllerApi( controller );
	}

	private static Window lastSeenWindow = null;

	public static Window use( Window window )
	{
		if( window instanceof Stage )
		{
			( ( Stage )window ).toFront();
		}
		return lastSeenWindow = window;
	}

	public static Object offset( Object target, double offsetX, double offsetY )
	{
		return new OffsetTarget( target, offsetX, offsetY );
	}

	public static Set<Node> findAll( String selector, Object parent )
	{
		if( parent instanceof Node )
		{
			Node node = ( Node )parent;
			use( node.getScene().getWindow() );
			return node.lookupAll( selector );
		}
		else if( parent instanceof Scene )
		{
			Scene scene = ( Scene )parent;
			use( scene.getWindow() );
			return Collections.singleton( scene.lookup( selector ) );
		}
		else if( parent instanceof Window )
		{
			return findAll( selector, use( ( Window )parent ).getScene() );
		}

		return Collections.emptySet();
	}

	public static Set<Node> findAll( String selector )
	{
		return findAll( selector, lastSeenWindow );
	}

	public static Node find( String selector, Object parent )
	{
		return Preconditions.checkNotNull( Iterables.getFirst( findAll( selector, parent ), null ),
				"Query [%s] select [%s] resulted in no nodes found!", parent, selector );
	}

	public static Node find( String selector )
	{
		return find( selector, lastSeenWindow );
	}

	private final ScreenController controller;
	private final Set<MouseButton> pressedButtons = new HashSet<>();
	private final Set<KeyCode> pressedKeys = new HashSet<>();

	public ControllerApi( ScreenController controller )
	{
		this.controller = controller;
	}

	public ControllerApi click( MouseButton... buttons )
	{
		if( buttons.length == 0 )
		{
			return click( MouseButton.PRIMARY );
		}

		press( buttons );
		return release( buttons );
	}

	public ControllerApi click( Object target, MouseButton... buttons )
	{
		move( target );
		return click( buttons );
	}

	public MouseMotion drag( Object source, MouseButton... buttons )
	{
		move( source );
		press( buttons );

		return new MouseMotion( buttons );
	}

	public ControllerApi move( double x, double y )
	{
		controller.move( x, y );
		return this;
	}

	public ControllerApi move( Object target )
	{
		Point2D point = pointFor( target );
		return move( point.getX(), point.getY() );
	}

	public ControllerApi moveBy( double x, double y )
	{
		Point2D mouse = controller.getMouse();
		controller.move( mouse.getX() + x, mouse.getY() + y );
		return this;
	}

	public ControllerApi press( MouseButton... buttons )
	{
		if( buttons.length == 0 )
		{
			return press( MouseButton.PRIMARY );
		}

		for( MouseButton button : buttons )
		{
			if( pressedButtons.add( button ) )
			{
				controller.press( button );
			}
		}
		return this;
	}

	public ControllerApi release( MouseButton... buttons )
	{
		if( buttons.length == 0 )
		{
			for( MouseButton button : pressedButtons )
			{
				controller.release( button );
			}
			pressedButtons.clear();
		}
		else
		{
			for( MouseButton button : buttons )
			{
				if( pressedButtons.remove( button ) )
				{
					controller.release( button );
				}
			}
		}
		return this;
	}

	public ControllerApi type( KeyCode... keys )
	{
		press( keys );
		return release( keys );
	}

	public ControllerApi press( KeyCode... keys )
	{
		for( KeyCode key : keys )
		{
			if( pressedKeys.add( key ) )
			{
				controller.press( key );
			}
		}
		return this;
	}

	public ControllerApi release( KeyCode... keys )
	{
		if( keys.length == 0 )
		{
			for( KeyCode button : pressedKeys )
			{
				controller.release( button );
			}
			pressedKeys.clear();
		}
		else
		{
			for( KeyCode key : keys )
			{
				if( pressedKeys.remove( key ) )
				{
					controller.release( key );
				}
			}
		}
		return this;
	}

	private Pos nodePosition = Pos.CENTER;

	public ControllerApi pos( Pos pos )
	{
		nodePosition = pos;
		return this;
	}

	private Point2D pointForBounds( Bounds bounds )
	{
		double x = 0;
		switch( nodePosition.getHpos() )
		{
		case LEFT :
			x = bounds.getMinX();
			break;
		case CENTER :
			x = ( bounds.getMinX() + bounds.getMaxX() ) / 2;
			break;
		case RIGHT :
			x = bounds.getMaxX();
			break;
		}

		double y = 0;
		switch( nodePosition.getVpos() )
		{
		case TOP :
			y = bounds.getMinY();
			break;
		case CENTER :
		case BASELINE :
			y = ( bounds.getMinY() + bounds.getMaxY() ) / 2;
			break;
		case BOTTOM :
			y = bounds.getMaxY();
			break;
		}

		return new Point2D( x, y );
	}

	private static Bounds sceneBoundsToScreenBounds( Bounds sceneBounds, Scene scene )
	{
		Window window = use( scene.getWindow() );
		return new BoundingBox( window.getX() + scene.getX() + sceneBounds.getMinX(), window.getY() + scene.getY()
				+ sceneBounds.getMinY(), sceneBounds.getWidth(), sceneBounds.getHeight() );
	}

	public Point2D pointFor( Object target )
	{
		if( target instanceof Point2D )
		{
			return ( Point2D )target;
		}
		else if( target instanceof Bounds )
		{
			return pointForBounds( ( Bounds )target );
		}
		else if( target instanceof Node )
		{
			Node node = ( Node )target;
			return pointFor( sceneBoundsToScreenBounds( node.localToScene( node.getBoundsInLocal() ), node.getScene() ) );
		}
		else if( target instanceof Scene )
		{
			Scene scene = ( Scene )target;
			return pointFor( sceneBoundsToScreenBounds( new BoundingBox( 0, 0, scene.getWidth(), scene.getHeight() ),
					scene ) );
		}
		else if( target instanceof Window )
		{
			Window window = use( ( Window )target );
			return pointFor( new BoundingBox( window.getX(), window.getY(), window.getWidth(), window.getHeight() ) );
		}
		else if( target instanceof OffsetTarget )
		{
			OffsetTarget offset = ( OffsetTarget )target;
			Pos oldPos = nodePosition;
			Point2D targetPoint = pos( Pos.TOP_LEFT ).pointFor( offset.target );
			pos( oldPos );
			return new Point2D( targetPoint.getX() + offset.offsetX, targetPoint.getY() + offset.offsetY );
		}

		throw new IllegalArgumentException( "Unable to get coordinates for: " + target );
	}

	private static class OffsetTarget
	{
		private final Object target;
		private final double offsetX;
		private final double offsetY;

		private OffsetTarget( Object target, double offsetX, double offsetY )
		{
			this.target = target;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}
	}

	public class MouseMotion
	{
		private final MouseButton[] buttons;

		private MouseMotion( MouseButton... buttons )
		{
			this.buttons = buttons;
		}

		public ControllerApi to( double x, double y )
		{
			move( x, y );
			return release( buttons );
		}

		public ControllerApi to( Object target )
		{
			move( target );
			return release( buttons );
		}

		public MouseMotion via( double x, double y )
		{
			move( x, y );
			return this;
		}

		public MouseMotion via( Object target )
		{
			move( target );
			return this;
		}

		public MouseMotion by( double x, double y )
		{
			moveBy( x, y );
			return this;
		}

		public ControllerApi drop()
		{
			return release( buttons );
		}
	}
}
