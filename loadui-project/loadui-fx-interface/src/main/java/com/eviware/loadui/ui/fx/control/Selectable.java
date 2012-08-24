package com.eviware.loadui.ui.fx.control;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Selectable
{
	private static final Logger log = LoggerFactory.getLogger( Selectable.class );
	private static final SelectionHandler SELECTION_HANDLER = new SelectionHandler();
	private static final DeselectionHandler DESELECTION_HANDLER = new DeselectionHandler();
	private static final Set<Node> SELECTED_NODES = Collections.newSetFromMap( new WeakHashMap<Node, Boolean>() );

	/**
	 * Makes the node selectable by clicking anywhere on the node.
	 * 
	 * @param node
	 * @return
	 */
	public static void installSelectable( @Nonnull Node node )
	{
		node.addEventHandler( MouseEvent.MOUSE_PRESSED, SELECTION_HANDLER );
	}

	public static void installClearSelectionArea( @Nonnull Node node )
	{
		node.addEventHandler( MouseEvent.MOUSE_PRESSED, DESELECTION_HANDLER );
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
}
