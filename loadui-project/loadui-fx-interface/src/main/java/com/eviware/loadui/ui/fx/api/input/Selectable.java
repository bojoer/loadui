package com.eviware.loadui.ui.fx.api.input;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import javafx.application.Platform;
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

import com.google.common.collect.ImmutableList;

public class Selectable
{
	private static final Logger log = LoggerFactory.getLogger( Selectable.class );
	private static final ClickToSelectHandler SELECTION_HANDLER = new ClickToSelectHandler();
	private static final ClickToDeselectHandler DESELECTION_HANDLER = new ClickToDeselectHandler();
	private static final Set<Node> SELECTED_NODES = Collections.newSetFromMap( new WeakHashMap<Node, Boolean>() );
	private static ImmutableList<Node> selectedNodesAtSelectionStart = null;
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
				selectionRectangle.startSelection( event );
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
					Platform.runLater( new Runnable()
					{
						@Override
						public void run()
						{
							selectionRectangle.hide();
							selectionRectangle = null;
						}
					} );
				}
				else if( selectionRectangle != null )
				{
					selectionRectangle.updateSelection( event, selectables );
				}
			}
		} );
		selectionArea.addEventHandler( MouseEvent.MOUSE_CLICKED, DESELECTION_HANDLER );
	}

	private static void select( Node n )
	{
		n.setEffect( new Glow( 0.8 ) );
		SELECTED_NODES.add( n );
	}

	private static void selectAll( Collection<Node> nodes )
	{
		for( Node n : nodes )
			select( n );
	}

	private static void deselectAll()
	{
		for( Iterator<Node> i = SELECTED_NODES.iterator(); i.hasNext(); )
		{
			Node n = i.next();
			n.setEffect( null );
			i.remove();
		}
	}

	private static void deselect( Node n )
	{
		n.setEffect( null );
		SELECTED_NODES.remove( n );
	}

	private static class ClickToSelectHandler implements EventHandler<MouseEvent>
	{
		@Override
		public void handle( MouseEvent event )
		{
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

	private static class ClickToDeselectHandler implements EventHandler<MouseEvent>
	{
		@Override
		public void handle( MouseEvent event )
		{
			deselectAll();
			event.consume();
		}
	}

	private static class SelectionRectangle extends Popup
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

		void startSelection( MouseEvent e )
		{
			selectedNodesAtSelectionStart = ImmutableList.copyOf( SELECTED_NODES );
			if( !e.isControlDown() && !e.isShiftDown() )
				deselectAll();
			this.startX = e.getScreenX();
			this.startY = e.getScreenY();
		}

		void updateSelection( MouseEvent e, Collection<? extends Region> selectables )
		{
			double width = Math.abs( e.getScreenX() - startX );
			double height = Math.abs( e.getScreenY() - startY );
			setWidth( width );
			setHeight( height );
			box.setPrefSize( width, height );

			show( ownerNode, Math.min( startX, e.getScreenX() ), Math.min( startY, e.getScreenY() ) );

			if( e.isShiftDown() || e.isControlDown() )
				selectAll( selectedNodesAtSelectionStart );
			else
				deselectAll();

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

					if( selectionArea.intersects( selectableRectangle ) )
					{
						if( ( e.isShiftDown() || e.isControlDown() ) && selectedNodesAtSelectionStart.contains( selectable ) )
							deselect( selectable );
						else
							select( selectable );
					}
				}
			}
		}
	}
}
