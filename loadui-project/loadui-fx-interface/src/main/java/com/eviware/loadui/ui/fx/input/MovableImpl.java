package com.eviware.loadui.ui.fx.input;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.input.Movable;
import com.eviware.loadui.ui.fx.util.NodeUtils;

/**
 * Adds the ability to move a Node by dragging and dropping with the mouse.
 * TODO: Add Bounds for limiting the movable space.
 * 
 * @author dain.nilsson
 */
public class MovableImpl implements Movable
{
	protected static final Logger log = LoggerFactory.getLogger( MovableImpl.class );

	private static final String MOVABLE_PROP_KEY = MovableImpl.class.getName();

	private static final MovableBehavior BEHAVIOR = new MovableBehavior();

	/**
	 * Makes the node movable by click-dragging the handle.
	 * 
	 * @param node
	 * @param handle
	 * @return
	 */
	public static MovableImpl install( Node node, Node handle )
	{
		return BEHAVIOR.install( node, handle );
	}

	/**
	 * Makes the node movable by click-dragging anywhere on the node.
	 * 
	 * @param node
	 * @return
	 */
	public static MovableImpl install( Node node )
	{
		return BEHAVIOR.install( node, node );
	}

	/**
	 * Removes the ability to move the node.
	 * 
	 * @param node
	 */
	public static void uninstall( Node node )
	{
		BEHAVIOR.uninstall( node );
	}

	private final Node node;
	private final Node handle;
	private Node currentlyHovered;
	private Point2D startPoint = new Point2D( 0, 0 );

	private MovableImpl( Node node, Node handle )
	{
		this.node = node;
		this.handle = handle;
	}

	public Node getNode()
	{
		return node;
	}

	public Node getHandle()
	{
		return handle;
	}

	private ReadOnlyBooleanWrapper draggingProperty;

	private ReadOnlyBooleanWrapper draggingPropertyImpl()
	{
		if( draggingProperty == null )
		{
			draggingProperty = new ReadOnlyBooleanWrapper( false );
		}

		return draggingProperty;
	}

	private void setDragging( boolean dragging )
	{
		if( isDragging() != dragging )
		{
			draggingPropertyImpl().set( dragging );
		}
	}

	@Override
	public ReadOnlyBooleanProperty draggingProperty()
	{
		return draggingPropertyImpl().getReadOnlyProperty();
	}

	@Override
	public boolean isDragging()
	{
		return draggingProperty == null ? false : draggingProperty.get();
	}

	private ReadOnlyBooleanWrapper acceptableProperty;

	private ReadOnlyBooleanWrapper acceptablePropertyImpl()
	{
		if( acceptableProperty == null )
		{
			acceptableProperty = new ReadOnlyBooleanWrapper( false );
		}

		return acceptableProperty;
	}

	private void setAcceptable( boolean acceptable )
	{
		if( isAcceptable() != acceptable )
		{
			acceptablePropertyImpl().set( acceptable );
		}
	}

	@Override
	public ReadOnlyBooleanProperty acceptableProperty()
	{
		return acceptablePropertyImpl().getReadOnlyProperty();
	}

	@Override
	public boolean isAcceptable()
	{
		return acceptableProperty == null ? false : acceptableProperty.get();
	}

	private ObjectProperty<Object> dataProperty;

	@Override
	public ObjectProperty<Object> dataProperty()
	{
		if( dataProperty == null )
		{
			dataProperty = new SimpleObjectProperty<>( this, "data" );
		}

		return dataProperty;
	}

	@Override
	public void setData( Object data )
	{
		dataProperty().set( data );
	}

	@Override
	public Object getData()
	{
		return dataProperty == null ? null : dataProperty.get();
	}

	private static class MovableBehavior
	{
		private double lastDraggedX = Double.NaN;
		private double lastDraggedY = Double.NaN;

		private final EventHandler<MouseEvent> pressedHandler = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				log.debug( "pressedHandler" );

				Node source = ( Node )event.getSource();
				MovableImpl movable = ( MovableImpl )source.getProperties().get( MOVABLE_PROP_KEY );
				if( movable != null )
				{
					movable.startPoint = new Point2D( event.getSceneX(), event.getSceneY() );
					movable.setDragging( true );
				}
			}
		};

		private final EventHandler<MouseEvent> draggedHandler = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				log.debug( "draggedHandler" );

				Node source = ( Node )event.getSource();
				final MovableImpl movable = ( MovableImpl )source.getProperties().get( MOVABLE_PROP_KEY );
				if( movable != null && movable.isDragging() )
				{
					Node node = movable.getNode();
					Point2D scenePoint = new Point2D( event.getSceneX(), event.getSceneY() );
					Point2D parentPoint = node.getParent().sceneToLocal( scenePoint );
					Point2D parentStartPoint = node.getParent().sceneToLocal( movable.startPoint );
					node.setTranslateX( parentPoint.getX() - parentStartPoint.getX() );
					node.setTranslateY( parentPoint.getY() - parentStartPoint.getY() );

					Node currentNode = NodeUtils.findFrontNodeAtCoordinate( source.getScene().getRoot(), scenePoint,
							movable.getNode(), movable.getHandle() );
					if( movable.currentlyHovered != currentNode )
					{
						movable.setAcceptable( false );
						if( movable.currentlyHovered != null )
						{
							movable.currentlyHovered.fireEvent( new DraggableEvent( null, node, movable.currentlyHovered,
									DraggableEvent.DRAGGABLE_EXITED, movable, event.getSceneX(), event.getSceneY() ) );
						}
						if( currentNode != null )
						{
							currentNode.fireEvent( new DraggableEvent( new Runnable()
							{
								@Override
								public void run()
								{
									movable.setAcceptable( true );
								}
							}, node, currentNode, DraggableEvent.DRAGGABLE_ENTERED, movable, event.getSceneX(), event
									.getSceneY() ) );
						}

						movable.currentlyHovered = currentNode;
						lastDraggedX = Double.NaN;
					}
					if( event.getSceneX() != lastDraggedX || event.getSceneY() != lastDraggedY )
					{
						lastDraggedX = event.getSceneX();
						lastDraggedY = event.getSceneY();
						node.fireEvent( new DraggableEvent( null, node, node, DraggableEvent.DRAGGABLE_DRAGGED, movable,
								lastDraggedX, lastDraggedY ) );
					}
				}
				event.consume();
			}
		};

		private final EventHandler<MouseEvent> releasedHandler = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				log.debug( "releasedHandler" );

				Node source = ( Node )event.getSource();
				MovableImpl movable = ( MovableImpl )source.getProperties().get( MOVABLE_PROP_KEY );
				if( movable != null && movable.isDragging() )
				{
					Node node = movable.getNode();
					node.setLayoutX( node.getLayoutX() + node.getTranslateX() );
					node.setLayoutY( node.getLayoutY() + node.getTranslateY() );
					node.setTranslateX( 0 );
					node.setTranslateY( 0 );

					if( movable.currentlyHovered != null )
					{
						Point2D point = movable.currentlyHovered.sceneToLocal( event.getSceneX(), event.getSceneY() );
						movable.currentlyHovered.fireEvent( new DraggableEvent( null, node, movable.currentlyHovered,
								DraggableEvent.DRAGGABLE_EXITED, movable, point.getX(), point.getY() ) );

						if( movable.isAcceptable() )
						{
							movable.currentlyHovered.fireEvent( new DraggableEvent( null, node, movable.currentlyHovered,
									DraggableEvent.DRAGGABLE_DROPPED, movable, point.getX(), point.getY() ) );
						}
					}

					movable.currentlyHovered = null;
					movable.setAcceptable( false );
					movable.setDragging( false );
				}
			}
		};

		private MovableImpl install( Node node, Node handle )
		{
			MovableImpl movable = new MovableImpl( node, handle );

			node.getProperties().put( MOVABLE_PROP_KEY, movable );
			handle.getProperties().put( MOVABLE_PROP_KEY, movable );

			handle.addEventHandler( MouseEvent.MOUSE_PRESSED, pressedHandler );
			handle.addEventHandler( MouseEvent.MOUSE_DRAGGED, draggedHandler );
			handle.addEventHandler( MouseEvent.MOUSE_RELEASED, releasedHandler );

			return movable;
		}

		private void uninstall( Node node )
		{
			MovableImpl movable = ( MovableImpl )node.getProperties().remove( MOVABLE_PROP_KEY );
			if( movable != null )
			{
				Node handle = movable.getHandle();
				handle.removeEventHandler( MouseEvent.MOUSE_PRESSED, pressedHandler );
				handle.removeEventHandler( MouseEvent.MOUSE_DRAGGED, draggedHandler );
				handle.removeEventHandler( MouseEvent.MOUSE_RELEASED, releasedHandler );
				handle.getProperties().remove( MOVABLE_PROP_KEY );
			}
		}
	}

	public static boolean isMovable( Node node )
	{
		return node.getProperties().containsKey( MOVABLE_PROP_KEY );
	}

	static MovableImpl getMovable( Node node )
	{
		return ( MovableImpl )node.getProperties().get( MOVABLE_PROP_KEY );
	}
}
