package com.eviware.loadui.ui.fx.control;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TimelineBuilder;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PopupControl;
import javafx.scene.input.MouseEvent;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import com.eviware.loadui.ui.fx.api.input.Draggable;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.util.NodeUtils;

/**
 * Adds the ability to drag an object from a source Node, which can potentially
 * be dropped on a target.
 * 
 * @author dain.nilsson
 */
public class DragNode extends PopupControl implements Draggable
{
	private static final Duration REVERT_DURATION = new Duration( 300 );
	private static final String DRAG_NODE_PROP_KEY = DragNode.class.getName();
	private static final DragNodeBehavior BEHAVIOR = new DragNodeBehavior();

	public static void install( Node node, DragNode dragNode )
	{
		BEHAVIOR.install( node, dragNode );
	}

	public static DragNode install( Node node, Node draggableNode )
	{
		DragNode dragNode = new DragNode( draggableNode );
		BEHAVIOR.install( node, dragNode );

		return dragNode;
	}

	public static void uninstall( Node node, DragNode dragNode )
	{
		BEHAVIOR.uninstall( node );
	}

	private final ObjectProperty<Node> nodeProperty = new SimpleObjectProperty<>();

	public ObjectProperty<Node> nodeProperty()
	{
		return nodeProperty;
	}

	public void setNode( Node content )
	{
		nodeProperty.set( content );
	}

	public Node getNode()
	{
		return nodeProperty.get();
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

	private BooleanProperty revertProperty;

	public BooleanProperty revertProperty()
	{
		if( revertProperty == null )
		{
			revertProperty = new SimpleBooleanProperty( this, "revert", true );
		}

		return revertProperty;
	}

	public boolean isRevert()
	{
		return revertProperty == null ? true : revertProperty.get();
	}

	public void setRevert( boolean revert )
	{
		revertProperty().set( revert );
	}

	private Node dragSource;
	private Node currentlyHovered;
	private Point2D startPoint = new Point2D( 0, 0 );
	private Point2D lastPoint = new Point2D( 0, 0 );

	public DragNode( Node node )
	{
		nodeProperty.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable observable )
			{
				bridge.getChildren().setAll( getNode() );
			}
		} );
		nodeProperty.set( node );
	}

	public Node getDragSource()
	{
		return dragSource;
	}

	private void revert()
	{
		if( !isRevert() || isAcceptable() )
		{
			hide();
			return;
		}

		DoubleProperty xProp = new SimpleDoubleProperty( getX() );
		xProp.addListener( new ChangeListener<Number>()
		{
			@Override
			public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
			{
				setX( newValue.doubleValue() );
			}
		} );
		DoubleProperty yProp = new SimpleDoubleProperty( getY() );
		yProp.addListener( new ChangeListener<Number>()
		{
			@Override
			public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
			{
				setY( newValue.doubleValue() );
			}
		} );

		TimelineBuilder
				.create()
				.keyFrames(
						new KeyFrame( REVERT_DURATION, new KeyValue( xProp, startPoint.getX(), Interpolator.EASE_BOTH ),
								new KeyValue( yProp, startPoint.getY(), Interpolator.EASE_BOTH ) ) )
				.onFinished( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						hide();
					}
				} ).build().playFromStart();
	}

	private static class DragNodeBehavior
	{
		private final EventHandler<MouseEvent> PRESSED_HANDLER = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				Node source = ( Node )event.getSource();
				DragNode dragNode = ( DragNode )source.getProperties().get( DRAG_NODE_PROP_KEY );
				if( dragNode != null )
				{
					dragNode.startPoint = new Point2D( event.getScreenX() - dragNode.getWidth() / 2, event.getScreenY()
							- dragNode.getHeight() / 2 );
					dragNode.show( source, dragNode.startPoint.getX(), dragNode.startPoint.getY() );
					dragNode.setDragging( true );
				}
			}
		};

		private final EventHandler<MouseEvent> DRAGGED_HANDLER = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				Node source = ( Node )event.getSource();
				final DragNode dragNode = ( DragNode )source.getProperties().get( DRAG_NODE_PROP_KEY );
				if( dragNode != null )
				{
					dragNode.setX( event.getScreenX() - dragNode.getWidth() / 2 );
					dragNode.setY( event.getScreenY() - dragNode.getHeight() / 2 );

					Window window = dragNode.getDragSource().getScene().getWindow();
					Point2D scenePoint = new Point2D( event.getSceneX(), event.getSceneY() );
					Node currentNode = null;
					while( currentNode == null && window != null )
					{
						Scene scene = window.getScene();
						scenePoint = new Point2D( event.getScreenX() - window.getX() - scene.getX(), event.getScreenY()
								- window.getY() - scene.getY() );
						currentNode = NodeUtils.findFrontNodeAtCoordinate( scene.getRoot(), scenePoint );

						if( window instanceof PopupWindow )
						{
							window = ( ( PopupWindow )window ).getOwnerWindow();
						}
						else if( window instanceof Stage )
						{
							window = ( ( Stage )window ).getOwner();
						}
						else
						{
							window = null;
						}
					}

					dragNode.lastPoint = scenePoint;

					if( dragNode.currentlyHovered != currentNode )
					{
						dragNode.setAcceptable( false );
						if( dragNode.currentlyHovered != null )
						{
							dragNode.currentlyHovered.fireEvent( new DraggableEvent( null, dragNode.getNode(),
									dragNode.currentlyHovered, DraggableEvent.DRAGGABLE_EXITED, dragNode.getData(), event
											.getSceneX(), event.getSceneY() ) );
						}
						if( currentNode != null )
						{
							currentNode.fireEvent( new DraggableEvent( new Runnable()
							{
								@Override
								public void run()
								{
									dragNode.setAcceptable( true );
								}
							}, dragNode.getNode(), currentNode, DraggableEvent.DRAGGABLE_ENTERED, dragNode.getData(),
									scenePoint.getX(), scenePoint.getY() ) );
						}

						dragNode.currentlyHovered = currentNode;
					}
				}
			}
		};

		private final EventHandler<MouseEvent> RELEASED_HANDLER = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				Node source = ( Node )event.getSource();
				final DragNode dragNode = ( DragNode )source.getProperties().get( DRAG_NODE_PROP_KEY );
				if( dragNode != null )
				{
					if( dragNode.currentlyHovered != null )
					{
						dragNode.currentlyHovered.fireEvent( new DraggableEvent( null, dragNode.getNode(),
								dragNode.currentlyHovered, DraggableEvent.DRAGGABLE_EXITED, dragNode.getData(), event
										.getSceneX(), event.getSceneY() ) );

						if( dragNode.isAcceptable() )
						{
							dragNode.currentlyHovered.fireEvent( new DraggableEvent( null, dragNode.getNode(),
									dragNode.currentlyHovered, DraggableEvent.DRAGGABLE_DROPPED, dragNode.getData(),
									dragNode.lastPoint.getX(), dragNode.lastPoint.getY() ) );
						}
					}

					dragNode.currentlyHovered = null;
					dragNode.revert();
					dragNode.setAcceptable( false );
					dragNode.setDragging( false );
				}
			}
		};

		private void install( Node node, DragNode dragNode )
		{
			if( node == null )
			{
				return;
			}

			dragNode.dragSource = node;
			node.getProperties().put( DRAG_NODE_PROP_KEY, dragNode );
			node.addEventHandler( MouseEvent.DRAG_DETECTED, PRESSED_HANDLER );
			node.addEventHandler( MouseEvent.MOUSE_DRAGGED, DRAGGED_HANDLER );
			node.addEventHandler( MouseEvent.MOUSE_RELEASED, RELEASED_HANDLER );
		}

		private void uninstall( Node node )
		{
			if( node == null )
			{
				return;
			}

			node.removeEventHandler( MouseEvent.DRAG_DETECTED, PRESSED_HANDLER );
			node.removeEventHandler( MouseEvent.MOUSE_DRAGGED, DRAGGED_HANDLER );
			node.removeEventHandler( MouseEvent.MOUSE_RELEASED, RELEASED_HANDLER );
			DragNode dragNode = ( DragNode )node.getProperties().remove( DRAG_NODE_PROP_KEY );
			if( dragNode != null && dragNode.isShowing() )
			{
				dragNode.hide();
			}
		}
	}
}
