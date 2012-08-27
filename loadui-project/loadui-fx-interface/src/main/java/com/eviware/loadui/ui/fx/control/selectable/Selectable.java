package com.eviware.loadui.ui.fx.control.selectable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Popup;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Selectable
{
	private static final Logger log = LoggerFactory.getLogger( Selectable.class );
	private static final SelectionHandler SELECTION_HANDLER = new SelectionHandler();
	private static final DeselectionHandler DESELECTION_HANDLER = new DeselectionHandler();
	private static final Set<Node> SELECTED_NODES = Collections.newSetFromMap( new WeakHashMap<Node, Boolean>() );
	private static SelectionRectangle selectionRectangle;

	/**
	 * Makes the node selectable by clicking anywhere on the node.
	 * 
	 * @param node
	 * @return
	 */
	public static void installSelectable( @Nonnull Node node )
	{
		node.addEventHandler( MouseEvent.MOUSE_CLICKED, SELECTION_HANDLER );
	}

	public static void installDragToSelectArea( @Nonnull final Node selectionArea,
			@Nonnull final Collection<? extends Region> selectables )
	{
		selectionArea.addEventHandler( MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				selectionRectangle = new SelectionRectangle( selectionArea );
				selectionRectangle.startSelection( event.getScreenX(), event.getScreenY() );
				log.debug( "Starting at (" + event.getScreenX() + "," + event.getScreenY() + ")" );
				selectionArea.startFullDrag();
			}
		} );

		selectionArea.addEventHandler( MouseDragEvent.ANY, new EventHandler<MouseDragEvent>()
		{
			@Override
			public void handle( MouseDragEvent event )
			{
				if( event.getEventType() == MouseDragEvent.MOUSE_DRAG_RELEASED )
				{
					selectionRectangle.endSelection();
					selectionRectangle = null;
				}
				else
				{
					log.debug( "Updating to (" + event.getScreenX() + "," + event.getScreenY() + ")" );
					if( selectionRectangle != null )
						selectionRectangle.updateSelection( event.getScreenX(), event.getScreenY(), selectables );
				}
			}
		} );

		selectionArea.addEventHandler( MouseEvent.MOUSE_CLICKED, DESELECTION_HANDLER );
	}

	private static void select( Node n )
	{
		log.debug( "Selecting " + n );
		n.setEffect( new Glow( 0.8 ) );
		SELECTED_NODES.add( n );
	}

	private static void deselectAll()
	{
		log.debug( "Deselecting all" );
		for( Iterator<Node> i = SELECTED_NODES.iterator(); i.hasNext(); )
		{
			Node n = i.next();
			n.setEffect( null );
			i.remove();
		}
	}

	private static void deselect( Node n )
	{
		log.debug( "Deselecting " + n );
		n.setEffect( null );
		SELECTED_NODES.remove( n );
	}

	private static class SelectionHandler implements EventHandler<MouseEvent>
	{
		@Override
		public void handle( MouseEvent event )
		{
			log.debug( "SelectionHandler" );

			Node source = ( Node )event.getSource();
			if( event.isShiftDown() || event.isControlDown() )
			{
				if( SELECTED_NODES.contains( source ) )
					deselect( source );
				else
					select( source );
			}
			else
			{
				deselectAll();
				select( source );
			}
			event.consume();
		}
	}

	private static class DeselectionHandler implements EventHandler<MouseEvent>
	{
		@Override
		public void handle( MouseEvent event )
		{
			log.debug( "Deselecting all selected nodes" );
			deselectAll();
			event.consume();
		}
	}

	public static class SelectionRectangle extends Popup
	{
		private double startX;
		private double startY;
		private final Node ownerNode;
		HBox box = new HBox();

		SelectionRectangle( Node ownerNode )
		{
			this.ownerNode = ownerNode;
			box.setStyle( "-fx-background-color: rgba(140, 140, 210, 0.5);" );
			getContent().add( box );
			setAutoFix( false );
		}

		void startSelection( double startX, double startY )
		{
			this.startX = startX;
			this.startY = startY;
		}

		void updateSelection( double currentX, double currentY, Collection<? extends Region> selectables )
		{
			double width = Math.abs( currentX - startX );
			double height = Math.abs( currentY - startY );
			setWidth( width );
			setHeight( height );
			box.setPrefSize( width, height );

			show( ownerNode, Math.min( startX, currentX ), Math.min( startY, currentY ) );

			Rectangle2D selectionArea = new Rectangle2D( getX(), getY(), width, height );

			for( Region selectable : selectables )
			{
				Scene scene = selectable.getScene();
				if( scene.getWindow().isFocused() )
				{
					Bounds selectableBounds = selectable.localToScene( selectable.getBoundsInLocal() );
					Rectangle2D selectableRectangle = new Rectangle2D( selectableBounds.getMinX() + scene.getX()
							+ scene.getWindow().getX(), selectableBounds.getMinY() + scene.getY() + scene.getWindow().getY(),
							selectable.getWidth(), selectable.getHeight() );

					log.debug( "Does " + selectionArea + " intersect " + selectableRectangle + " ?" );

					if( selectionArea.intersects( selectableRectangle ) )
						select( selectable );
				}
			}
		}

		void endSelection()
		{
			hide();
		}
	}
}
