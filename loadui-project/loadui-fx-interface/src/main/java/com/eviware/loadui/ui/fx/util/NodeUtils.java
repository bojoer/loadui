package com.eviware.loadui.ui.fx.util;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import com.eviware.loadui.api.traits.Releasable;

public final class NodeUtils
{
	public static Rectangle2D localToScreen( Node node, Scene scene )
	{
		Bounds selectableBounds = node.localToScene( node.getBoundsInLocal() );

		return new Rectangle2D( selectableBounds.getMinX() + scene.getX() + scene.getWindow().getX(),
				selectableBounds.getMinY() + scene.getY() + scene.getWindow().getY(), node.getBoundsInLocal().getWidth(),
				node.getBoundsInLocal().getHeight() );
	}

	public static String toBase64Image( BufferedImage bimg )
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			ImageIO.write( bimg, "png", baos );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return new Base64().encodeToString( baos.toByteArray() );
	}

	public static Image fromBase64Image( String base64 )
	{
		byte[] ba = Base64.decodeBase64( base64 );
		return new Image( new ByteArrayInputStream( ba ) );
	}

	public static Node findFrontNodeAtCoordinate( Node root, Point2D point, Node... ignored )
	{
		if( !root.contains( root.sceneToLocal( point ) ) || !root.isVisible() )
		{
			return null;
		}

		for( Node ignore : ignored )
		{
			if( isDescendant( ignore, root ) )
			{
				return null;
			}
		}

		if( root instanceof Parent )
		{
			List<Node> children = ( ( Parent )root ).getChildrenUnmodifiable();
			for( int i = children.size() - 1; i >= 0; i-- )
			{
				Node result = findFrontNodeAtCoordinate( children.get( i ), point, ignored );
				if( result != null )
				{
					return result;
				}
			}
		}

		return root;
	}

	public static boolean isDescendant( Node ancestor, Node descendant )
	{
		Node current = descendant;
		while( current != null )
		{
			if( current == ancestor )
			{
				return true;
			}

			current = current.getParent();
		}

		return false;
	}

	public static void bindStyleClass( @Nonnull final Node nodeToStyle, @Nonnull final String styleClass,
			ObservableBooleanValue value )
	{
		updateStyleClasses( nodeToStyle, styleClass, value.getValue() );
		value.addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue )
			{
				updateStyleClasses( nodeToStyle, styleClass, newValue );
			}

		} );
	}

	private static void updateStyleClasses( final Node nodeToStyle, final String styleClass, Boolean newValue )
	{
		if( newValue )
			nodeToStyle.getStyleClass().add( styleClass );
		else
			nodeToStyle.getStyleClass().remove( styleClass );
	}

	public static void releaseRecursive( Node node )
	{
		tryRelease( node );
		if( node instanceof Parent )
		{
			for( Node childNode : ( ( Parent )node ).getChildrenUnmodifiable() )
			{
				releaseRecursive( childNode );
			}
		}
	}

	private static void tryRelease( Node node )
	{
		if( node instanceof Releasable )
		{
			System.out.println( "!!!!! RELEASING " + node );
			( ( Releasable )node ).release();
		}
	}

	/**
	 * @param node
	 * @return true if and only if the mouse pointer is on the given Node. The
	 *         node's bounds are used to decide whether this is the case or not.
	 */
	public static boolean isMouseOn( Node node )
	{
		Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
		double windowX = node.getScene().getWindow().getX();
		double windowY = node.getScene().getWindow().getY();
		double sceneX = node.getScene().getX();
		double sceneY = node.getScene().getY();
		Bounds boundsInScene = node.localToScene( node.getBoundsInLocal() );
		Point2D mouseLocationInScene = new Point2D( mouseLocation.getX() - windowX - sceneX, mouseLocation.getY()
				- windowY - sceneY );
		boolean result = boundsInScene.contains( mouseLocationInScene );
		return result;
	}

}