/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.input;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Popup;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.ui.fx.api.input.Selectable;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class SelectableImpl implements Selectable
{
	protected static final Logger log = LoggerFactory.getLogger( SelectableImpl.class );
	private static final String selectablePropertyKey = SelectableImpl.class.getName();
	private static final PressToSelectHandler pressToSelectHandler = new PressToSelectHandler();
	private static final ClickToDeselectHandler clickToDeselectHandler = new ClickToDeselectHandler();
	private static final Set<SelectableImpl> currentlySelected = Collections
			.newSetFromMap( new WeakHashMap<SelectableImpl, Boolean>() );
	@Nonnull
	private static SelectionRectangle SELECTION_RECTANGLE = new SelectionRectangle();
	private static final Collection<Node> SELECTABLE_NODES = Collections
			.newSetFromMap( new WeakHashMap<Node, Boolean>() );
	private static final HideSelectionRectangle HIDE_SELECTION_RECTANGLE = new HideSelectionRectangle();
	private static boolean isDragging;

	private static final Function<SelectableImpl, Node> SELECTABLE_TO_NODE = new Function<SelectableImpl, Node>()
	{
		@Override
		public Node apply( SelectableImpl input )
		{
			return input.getNode();
		}
	};

	/**
	 * Makes the node selectable by clicking anywhere on the node.
	 * 
	 * @param node
	 * @return
	 */
	public static Selectable installSelectable( @Nonnull Node node )
	{
		Selectable selectable = new SelectableImpl( node );
		node.addEventHandler( MouseEvent.MOUSE_PRESSED, pressToSelectHandler );
		node.addEventHandler( MouseEvent.MOUSE_RELEASED, HIDE_SELECTION_RECTANGLE );
		node.getProperties().put( selectablePropertyKey, selectable );
		SELECTABLE_NODES.add( node );
		return selectable;
	}

	public static void uninstallSelectable( @Nonnull Node node )
	{
		log.debug( "uninstall selectable" );

		node.removeEventHandler( MouseEvent.MOUSE_PRESSED, pressToSelectHandler );
		node.removeEventHandler( MouseEvent.MOUSE_RELEASED, HIDE_SELECTION_RECTANGLE );
		node.getProperties().remove( selectablePropertyKey );
		SELECTABLE_NODES.remove( node );
	}

	public static void installDeleteKeyHandler( Node node )
	{
		node.addEventFilter( KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
		{
			@Override
			public void handle( KeyEvent event )
			{
				if( event.getCode() == KeyCode.DELETE && !event.isConsumed() )
				{
					for( Deletable deletable : Iterables.filter( Iterables.transform( getSelected(), SELECTABLE_TO_NODE ),
							Deletable.class ) )
					{
						( ( Node )deletable ).fireEvent( IntentEvent.create( IntentEvent.INTENT_DELETE, deletable ) );
					}
				}
			}
		} );
	}

	public static boolean isSelectable( Node node )
	{
		return node.getProperties().containsKey( selectablePropertyKey );
	}

	public static Selectable get( @Nonnull final Node node )
	{
		return ( Selectable )node.getProperties().get( selectablePropertyKey );
	}

	private ReadOnlyBooleanWrapper selectedProperty;
	private final Node node;

	private SelectableImpl( Node node )
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
		currentlySelected.add( this );
		setSelected( true );
	}

	public void deselect()
	{
		currentlySelected.remove( this );
		setSelected( false );
	}

	private void setSelected( boolean selected )
	{
		if( isSelected() != selected )
		{
			selectedPropertyImpl().set( selected );
		}
	}

	public static ImmutableSet<SelectableImpl> getSelected()
	{
		return ImmutableSet.copyOf( currentlySelected );
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
		selectionArea.addEventHandler( MouseEvent.MOUSE_RELEASED, clickToDeselectHandler );
	}

	private static void selectAll( Collection<SelectableImpl> selectables )
	{
		for( Selectable s : selectables )
			s.select();
	}

	public static void deselectAll()
	{
		for( Iterator<SelectableImpl> i = currentlySelected.iterator(); i.hasNext(); )
		{
			SelectableImpl s = i.next();
			s.setSelected( false );
			i.remove();
		}
	}

	private static Selectable nodeToSelectable( Node source )
	{
		return ( Selectable )source.getProperties().get( selectablePropertyKey );
	}

	private static final class HideSelectionRectangle implements EventHandler<MouseEvent>
	{
		@Override
		public void handle( final MouseEvent event )
		{
			log.debug( "hide selection rectangle" );

			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					Node node = ( Node )event.getSource();
					boolean isMoving = MovableImpl.isMovable( node ) && MovableImpl.getMovable( node ).isDragging();
					if( !SELECTION_RECTANGLE.isShowing() && !isMoving && !event.isShortcutDown() && !event.isShiftDown() )
					{
						deselectAll();
						if( SelectableImpl.isSelectable( node ) )
							SelectableImpl.get( node ).select();
					}
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
			log.debug( "press selectable" );

			Node source = ( Node )event.getSource();
			Selectable selectable = nodeToSelectable( source );

			if( event.isShiftDown() )
			{
				if( currentlySelected.contains( selectable ) )
					selectable.deselect();
				else
					selectable.select();
			}
			else if( currentlySelected.contains( selectable ) && currentlySelected.size() > 1 )
			{
				//System.out.println( "Waiting for multidrag..." );
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
			if( !isDragging && !event.isShortcutDown() )
			{
				deselectAll();
				event.consume();
			}
		}
	}

	private static class SelectionRectangle extends Popup
	{
		private static ImmutableList<SelectableImpl> selectedAtSelectionStart = null;
		private final Collection<Node> toBeCollected = Collections
				.newSetFromMap( new HashMap<Node, Boolean>() ); 
		private double startX;
		private double startY;
		private Node ownerNode;
		private final HBox box = new HBox();

		SelectionRectangle()
		{

			box.setStyle( "-fx-background-color: rgba(34, 68, 187, 0.5); -fx-border-color: rgba(34, 68, 187, 1.0); -fx-border-width: 1;" );
			getContent().add( box );
			setAutoFix( false );
		}

		public void setOwner( Region selectionArea )
		{
			this.ownerNode = selectionArea;
		}

		void startSelection( MouseEvent e )
		{
			selectedAtSelectionStart = ImmutableList.copyOf( currentlySelected );
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
				if( scene != null )
				{
					if( scene.windowProperty().get() != null )
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
					}else{
						toBeCollected.add( node );
					}
				}
			}
			for(Node node : toBeCollected){
				uninstallSelectable( node );
			}
			toBeCollected.clear();
		}
	}
}
