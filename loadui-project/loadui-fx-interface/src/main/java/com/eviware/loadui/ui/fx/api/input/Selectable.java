package com.eviware.loadui.ui.fx.api.input;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Popup;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.google.common.collect.ImmutableList;

public class Selectable
{
	@SuppressWarnings( "unused" )
	private static final Logger log = LoggerFactory.getLogger( Selectable.class );
	private static final String SELECTABLE_PROP_KEY = Selectable.class.getName();
	private static final ClickToSelectHandler CLICK_TO_SELECT_HANDLER = new ClickToSelectHandler();
	private static final ClickToDeselectHandler CLICK_TO_DESELECT_HANDLER = new ClickToDeselectHandler();
	private static final Set<Selectable> SELECTED_NODES = Collections
			.newSetFromMap( new WeakHashMap<Selectable, Boolean>() );
	private static ImmutableList<Selectable> selectedAtSelectionStart = null;
	private static SelectionRectangle selectionRectangle;
	private static final Collection<Node> selectableNodes = new HashSet<>();

	/**
	 * Makes the node selectable by clicking anywhere on the node.
	 * 
	 * @param node
	 * @return
	 */
	public static Selectable installSelectable( @Nonnull Node node )
	{
		Selectable selectable = new Selectable( node );
		node.addEventHandler( MouseEvent.MOUSE_CLICKED, CLICK_TO_SELECT_HANDLER );
		node.getProperties().put( SELECTABLE_PROP_KEY, selectable );
		selectableNodes.add( node );
		return selectable;
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
		SELECTED_NODES.add( this );
		setSelected( true );
	}

	public void deselect()
	{
		SELECTED_NODES.remove( this );
		setSelected( false );
		System.out.println( "deselect    " + node );
	}

	private void setSelected( boolean selected )
	{
		if( isSelected() != selected )
		{
			selectedPropertyImpl().set( selected );
		}
	}

	public static void installDragToSelectArea( @Nonnull final Region selectionArea )
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
							if( selectionRectangle != null )
								selectionRectangle.hide();
						}
					} );
				}
				else if( selectionRectangle != null )
				{
					selectionRectangle.updateSelection( event );
				}
			}
		} );

		selectionArea.addEventHandler( MouseEvent.MOUSE_CLICKED, CLICK_TO_DESELECT_HANDLER );
	}

	private static void selectAll( Collection<Selectable> selectables )
	{
		for( Selectable s : selectables )
			s.select();
	}

	private static void deselectAll()
	{
		for( Iterator<Selectable> i = SELECTED_NODES.iterator(); i.hasNext(); )
		{
			Selectable s = i.next();
			s.setSelected( false );
			i.remove();
			System.out.println( "deselect " + s.getNode() );
		}
	}

	private static Selectable nodeToSelectable( Node source )
	{
		return ( Selectable )source.getProperties().get( SELECTABLE_PROP_KEY );
	}

	private static class ClickToSelectHandler implements EventHandler<MouseEvent>
	{
		@Override
		public void handle( MouseEvent event )
		{
			Node source = ( Node )event.getSource();
			Selectable selectable = nodeToSelectable( source );

			if( event.isShiftDown() || event.isControlDown() )
			{
				if( SELECTED_NODES.contains( selectable ) )
					selectable.deselect();
				else
					selectable.select();
			}
			else
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
			System.out.println( "deselectAll" );
			deselectAll();
			if( selectionRectangle != null )
				selectionRectangle.hide();
			event.consume();
		}

	}

	private static class SelectionRectangle extends Popup
	{
		private double startX;
		private double startY;
		private final Node ownerNode;
		private final HBox box = new HBox();

		SelectionRectangle( Node ownerNode )
		{
			this.ownerNode = ownerNode;
			box.setStyle( "-fx-background-color: rgba(140, 140, 210, 0.5);" );
			getContent().add( box );
			setAutoFix( false );
		}

		void startSelection( MouseEvent e )
		{
			selectedAtSelectionStart = ImmutableList.copyOf( SELECTED_NODES );
			if( !e.isControlDown() && !e.isShiftDown() )
				deselectAll();
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

			if( e.isShiftDown() || e.isControlDown() )
				selectAll( selectedAtSelectionStart );
			else
				deselectAll();

			Rectangle2D selectionArea = new Rectangle2D( getX(), getY(), width, height );

			for( Node node : selectableNodes )
			{
				Scene scene = node.getScene();
				if( scene.getWindow().isFocused() )
				{
					Rectangle2D selectableRectangle = NodeUtils.localToScreen( node, scene );

					if( selectionArea.intersects( selectableRectangle ) )
					{
						Selectable selectable = nodeToSelectable( node );
						if( ( e.isShiftDown() || e.isControlDown() ) && selectedAtSelectionStart.contains( selectable ) )
							selectable.deselect();
						else
							selectable.select();
					}
				}
			}
		}

	}
}
