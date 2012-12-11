package com.eviware.loadui.ui.fx.api.input;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class MultiMovable
{
	protected static final Logger log = LoggerFactory.getLogger( MultiMovable.class );

	private static final String MOVA_ALONG_NODES_HANDLER_PROP_KEY = MultiMovable.class.getName()
			+ "MOVA_ALONG_NODES_HANDLER";

	private static final EventHandler<MouseEvent> UPDATE_COORDINATES_HANDLER = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle( MouseEvent event )
		{
			for( Selectable s : Selectable.getSelected() )
			{
				Node selectedNode = s.getNode();
				selectedNode.setLayoutX( selectedNode.getLayoutX() + selectedNode.getTranslateX() );
				selectedNode.setLayoutY( selectedNode.getLayoutY() + selectedNode.getTranslateY() );
				selectedNode.setTranslateX( 0 );
				selectedNode.setTranslateY( 0 );
			}
		}
	};

	private static final class MoveAlongNodesHandler implements EventHandler<DraggableEvent>
	{
		private final Movable movable;

		private MoveAlongNodesHandler( @Nonnull Movable movable )
		{
			this.movable = movable;
		}

		@Override
		public void handle( DraggableEvent event )
		{
			Node movedNode = movable.getNode();
			if( movable.isDragging() && event.getDraggable() == movable )
			{
				double translateX = movedNode.getTranslateX();
				double translateY = movedNode.getTranslateY();
				for( Selectable s : Selectable.getSelected() )
				{
					Node selectedNode = s.getNode();
					if( Movable.isMovable( selectedNode ) && Movable.getMovable( selectedNode ) != event.getDraggable() )
					{
						selectedNode.setTranslateX( translateX );
						selectedNode.setTranslateY( translateY );
						selectedNode.fireEvent( event );
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param selectionArea
	 *           Selection area that must already be installed using Selectable.
	 * @param movable
	 *           The node must already be Movable and Selectable.
	 */

	public static void install( @Nonnull final Region selectionArea, @Nonnull final Node node )
	{
		Preconditions.checkArgument( Selectable.isSelectable( node ), "The node must already be Selectable." );
		Preconditions.checkArgument( Movable.isMovable( node ), "The node must already be Movable." );

		final Movable movable = Movable.getMovable( node );
		MoveAlongNodesHandler handler = new MoveAlongNodesHandler( movable );
		node.getProperties().put( MOVA_ALONG_NODES_HANDLER_PROP_KEY, handler );
		node.addEventHandler( DraggableEvent.DRAGGABLE_DRAGGED, handler );
		node.addEventHandler( MouseEvent.MOUSE_RELEASED, UPDATE_COORDINATES_HANDLER );
	}

	@SuppressWarnings( "unchecked" )
	public static void uninstall( @Nonnull final Region selectionArea, @Nonnull final Node node )
	{
		log.debug( "uninstall multimovable" );
		if( Movable.isMovable( node ) )
		{
			final Movable movable = Movable.getMovable( node );
			node.removeEventHandler( DraggableEvent.DRAGGABLE_DRAGGED, ( EventHandler<DraggableEvent> )node
					.getProperties().get( MOVA_ALONG_NODES_HANDLER_PROP_KEY ) );
		}
		node.removeEventHandler( MouseEvent.MOUSE_RELEASED, UPDATE_COORDINATES_HANDLER );
	}
}
