package com.eviware.loadui.ui.fx.api.input;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Popup;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class Selectable
{
	protected static final Logger log = LoggerFactory.getLogger( Selectable.class );
	private static final String SELECTABLE_PROP_KEY = Selectable.class.getName();
	private static final PressToSelectHandler PRESS_TO_SELECT_HANDLER = new PressToSelectHandler();
	private static final ClickToDeselectHandler CLICK_TO_DESELECT_HANDLER = new ClickToDeselectHandler();
	private static final Set<Selectable> CURRENTLY_SELECTED = Collections
			.newSetFromMap( new WeakHashMap<Selectable, Boolean>() );
	@Nonnull
	private static SelectionRectangle SELECTION_RECTANGLE = new SelectionRectangle();
	private static final Collection<Node> SELECTABLE_NODES = Collections
			.newSetFromMap( new WeakHashMap<Node, Boolean>() );
	private static final HideSelectionRectangle HIDE_SELECTION_RECTANGLE = new HideSelectionRectangle();
	private static boolean isDragging;

	/**
	 * Makes the node selectable by clicking anywhere on the node.
	 * 
	 * @param node
	 * @return
	 */
	public static Selectable installSelectable( @Nonnull Node node )
	{
		Selectable selectable = new Selectable( node );
		node.addEventHandler( MouseEvent.MOUSE_PRESSED, PRESS_TO_SELECT_HANDLER );
		node.addEventHandler( MouseEvent.MOUSE_RELEASED, HIDE_SELECTION_RECTANGLE );
		node.getProperties().put( SELECTABLE_PROP_KEY, selectable );
		SELECTABLE_NODES.add( node );
		return selectable;
	}

	public static boolean isSelectable( Node node )
	{
		return node.getProperties().containsKey( SELECTABLE_PROP_KEY );
	}

	public static Selectable get( @Nonnull final Node node )
	{
		return ( Selectable )node.getProperties().get( SELECTABLE_PROP_KEY );
	}

	private ReadOnlyBooleanWrapper selectedProperty;
	private final Node node;

	private Selectable( Node node )
	{
		this.node = node;
	}

	private ReadOnlyBooleanWrapper selectedPropertyImpl()
	{
		if( selectedProperty == null )
		{
			selectedProperty = new ReadOnlyBooleanWrapper( false );
		}
		return selectedProperty;
	}

	public Node getNode()
	{
		return node;
	}

	public ReadOnlyBooleanProperty selectedProperty()
	{
		return selectedPropertyImpl().getReadOnlyProperty();
	}

	public boolean isSelected()
	{
		return selectedProperty == null ? false : selectedProperty.get();
	}

	public void select()
	{
		CURRENTLY_SELECTED.add( this );
		setSelected( true );
		log.debug( "Was selected." );
	}

	public void deselect()
	{
		log.debug( "deselect" );
		CURRENTLY_SELECTED.remove( this );
		setSelected( false );
	}

	private void setSelected( boolean selected )
	{
		if( isSelected() != selected )
		{
			selectedPropertyImpl().set( selected );
		}
	}

	public static ImmutableSet<Selectable> getSelected()
	{
		return ImmutableSet.copyOf( CURRENTLY_SELECTED );
	}

	public static void installDragToSelectArea( @Nonnull final Region selectionArea )
	{
		selectionArea.addEventHandler( MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( !event.isShortcutDown() )
				{
					isDragging = true;
					SELECTION_RECTANGLE.setOwner( selectionArea );
					SELECTION_RECTANGLE.startSelection( event );
				}
			}
		} );

		selectionArea.addEventHandler( MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( isDragging )
				{
					SELECTION_RECTANGLE.updateSelection( event );
				}
			}
		} );

		selectionArea.addEventHandler( MouseEvent.MOUSE_RELEASED, HIDE_SELECTION_RECTANGLE );
		selectionArea.addEventHandler( MouseEvent.MOUSE_RELEASED, CLICK_TO_DESELECT_HANDLER );
	}

	private static void selectAll( Collection<Selectable> selectables )
	{
		for( Selectable s : selectables )
			s.select();
	}

	private static void deselectAll()
	{
		System.out.println( "deselectAll" );
		for( Iterator<Selectable> i = CURRENTLY_SELECTED.iterator(); i.hasNext(); )
		{
			Selectable s = i.next();
			s.setSelected( false );
			i.remove();
		}
	}

	private static Selectable nodeToSelectable( Node source )
	{
		return ( Selectable )source.getProperties().get( SELECTABLE_PROP_KEY );
	}

	private static final class HideSelectionRectangle implements EventHandler<MouseEvent>
	{
		@Override
		public void handle( MouseEvent event )
		{
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					SELECTION_RECTANGLE.hide();
					isDragging = false;
				}
			} );
			event.consume();
		}
	}

	private static class PressToSelectHandler implements EventHandler<MouseEvent>
	{
		@Override
		public void handle( MouseEvent event )
		{
			Node source = ( Node )event.getSource();
			Selectable selectable = nodeToSelectable( source );

			if( event.isShiftDown() )
			{
				if( CURRENTLY_SELECTED.contains( selectable ) )
					selectable.deselect();
				else
					selectable.select();
			}
			else if( CURRENTLY_SELECTED.contains( selectable ) && CURRENTLY_SELECTED.size() > 1 )
			{
				System.out.println( "Waiting for multidrag..." );
			}
			else if( !isDragging && !event.isShortcutDown() )
			{
				deselectAll();
				selectable.select();
			}
			event.consume();
		}
	}

	private static class ClickToDeselectHandler implements EventHandler<MouseEvent>
	{
		@Override
		public void handle( MouseEvent event )
		{
			System.out.println( "ClickToDeselectHandler 1" );
			if( !isDragging && !event.isShortcutDown() )
			{
				System.out.println( "ClickToDeselectHandler 2" );
				deselectAll();
				event.consume();
			}
		}
	}

	private static class SelectionRectangle extends Popup
	{
		private static ImmutableList<Selectable> selectedAtSelectionStart = null;
		private double startX;
		private double startY;
		private Node ownerNode;
		private final HBox box = new HBox();

		SelectionRectangle()
		{
			box.setStyle( "-fx-background-color: rgba(140, 140, 210, 0.5);" );
			getContent().add( box );
			setAutoFix( false );
		}

		public void setOwner( Region selectionArea )
		{
			this.ownerNode = selectionArea;
		}

		void startSelection( MouseEvent e )
		{
			selectedAtSelectionStart = ImmutableList.copyOf( CURRENTLY_SELECTED );
			//			if( !e.isShiftDown() )
			//				deselectAll();
			this.startX = e.getScreenX();
			this.startY = e.getScreenY();
		}

		void updateSelection( MouseEvent e )
		{
			double width = Math.abs( e.getScreenX() - startX );
			double height = Math.abs( e.getScreenY() - startY );
			setWidth( width );
			setHeight( height );
			box.setPrefSize( width, height );

			show( ownerNode, Math.min( startX, e.getScreenX() ), Math.min( startY, e.getScreenY() ) );

			if( e.isShiftDown() )
				selectAll( selectedAtSelectionStart );
			else
				deselectAll();

			Rectangle2D selectionArea = new Rectangle2D( getX(), getY(), width, height );

			for( Node node : SELECTABLE_NODES )
			{
				Scene scene = node.getScene();
				if( scene.getWindow().isFocused() )
				{
					Rectangle2D selectableRectangle = NodeUtils.localToScreen( node, scene );

					if( selectionArea.intersects( selectableRectangle ) )
					{
						Selectable selectable = nodeToSelectable( node );
						if( e.isShiftDown() && selectedAtSelectionStart.contains( selectable ) )
							selectable.deselect();
						else
							selectable.select();
					}
				}
			}
		}
	}
}