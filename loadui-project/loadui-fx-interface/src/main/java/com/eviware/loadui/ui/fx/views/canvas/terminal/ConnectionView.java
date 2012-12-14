package com.eviware.loadui.ui.fx.views.canvas.terminal;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;

import com.eviware.loadui.api.events.TerminalConnectionEvent;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.views.canvas.CanvasObjectView;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ConnectionView extends Wire implements Deletable
{
	private final Connection connection;
	private final OutputTerminalView outputTerminalView;
	private final InputTerminalView inputTerminalView;

	public ConnectionView( final Connection connection, final CanvasObjectView outputComponentView,
			final CanvasObjectView inputComponentView )
	{
		super();

		getStyleClass().add( "connection-view" );

		this.connection = connection;

		final OutputTerminal outputTerminal = connection.getOutputTerminal();
		outputTerminalView = Iterables.find( outputComponentView.getOutputTerminalViews(),
				new Predicate<OutputTerminalView>()
				{
					@Override
					public boolean apply( OutputTerminalView input )
					{
						return input.getTerminal().equals( outputTerminal );
					}
				} );

		final InputTerminal inputTerminal = connection.getInputTerminal();
		inputTerminalView = Iterables.find( inputComponentView.getInputTerminalViews(),
				new Predicate<InputTerminalView>()
				{
					@Override
					public boolean apply( InputTerminalView input )
					{
						return input.getTerminal().equals( inputTerminal );
					}
				} );

		Runnable redraw = new Runnable()
		{
			@Override
			public void run()
			{
				requestLayout();
			}
		};

		outputTerminalView.setOnLayout( redraw );
		inputTerminalView.setOnLayout( redraw );

		final EventHandler<DraggableEvent> eventHandler = new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent arg0 )
			{
				updateWire();
			}
		};
		outputComponentView.addEventHandler( DraggableEvent.DRAGGABLE_DRAGGED, eventHandler );
		inputComponentView.addEventHandler( DraggableEvent.DRAGGABLE_DRAGGED, eventHandler );

		//Remove listeners when the Connection is disconnected:
		outputTerminal.addEventListener( TerminalConnectionEvent.class,
				new com.eviware.loadui.api.events.EventHandler<TerminalConnectionEvent>()
				{
					@Override
					public void handleEvent( TerminalConnectionEvent event )
					{
						if( Objects.equal( event.getConnection(), connection )
								&& event.getEvent() == TerminalConnectionEvent.Event.DISCONNECT )
						{
							outputComponentView.removeEventHandler( DraggableEvent.DRAGGABLE_DRAGGED, eventHandler );
							inputComponentView.removeEventHandler( DraggableEvent.DRAGGABLE_DRAGGED, eventHandler );
							outputTerminal.removeEventListener( TerminalConnectionEvent.class, this );
						}
					}
				} );
	}

	private void updateWire()
	{
		Bounds startBounds = sceneToLocal( outputTerminalView.localToScene( outputTerminalView.getBoundsInLocal() ) );
		Bounds endBounds = sceneToLocal( inputTerminalView.localToScene( inputTerminalView.getBoundsInLocal() ) );

		double startX = ( startBounds.getMaxX() + startBounds.getMinX() ) / 2;
		double startY = ( startBounds.getMaxY() + startBounds.getMinY() ) / 2;
		double endX = ( endBounds.getMaxX() + endBounds.getMinX() ) / 2;
		double endY = ( endBounds.getMaxY() + endBounds.getMinY() ) / 2;

		updatePosition( startX, startY, endX, endY );
	}

	@Override
	protected void layoutChildren()
	{
		updateWire();
		super.layoutChildren();
	}

	public Connection getConnection()
	{
		return connection;
	}

	public OutputTerminalView getOutputTerminalView()
	{
		return outputTerminalView;
	}

	public InputTerminalView getInputTerminalView()
	{
		return inputTerminalView;
	}

	@Override
	public void delete()
	{
		connection.disconnect();
	}
}
