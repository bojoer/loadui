package com.eviware.loadui.ui.fx.views.canvas.terminal;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;

import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.views.canvas.CanvasObjectView;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ConnectionView extends Region
{
	private final Connection connection;
	private final OutputTerminalView outputTerminalView;
	private final InputTerminalView inputTerminalView;
	private final Line wire = new Line();

	public ConnectionView( Connection connection, CanvasObjectView outputComponentView,
			CanvasObjectView inputComponentView )
	{
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

		EventHandler<DraggableEvent> eventHandler = new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent arg0 )
			{
				updateWire();
			}
		};
		outputComponentView.addEventHandler( DraggableEvent.DRAGGABLE_DRAGGED, eventHandler );
		inputComponentView.addEventHandler( DraggableEvent.DRAGGABLE_DRAGGED, eventHandler );

		getChildren().add( wire );

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				updateWire();
			}
		} );
	}

	private void updateWire()
	{
		Bounds startBounds = sceneToLocal( outputTerminalView.localToScene( outputTerminalView.getBoundsInLocal() ) );
		Bounds endBounds = sceneToLocal( inputTerminalView.localToScene( inputTerminalView.getBoundsInLocal() ) );

		System.out.println( "Draw wire from: " + startBounds + ", to: " + endBounds );
		wire.setStartX( ( startBounds.getMaxX() + startBounds.getMinX() ) / 2 );
		wire.setStartY( ( startBounds.getMaxY() + startBounds.getMinY() ) / 2 );
		wire.setEndX( ( endBounds.getMaxX() + endBounds.getMinX() ) / 2 );
		wire.setEndY( ( endBounds.getMaxY() + endBounds.getMinY() ) / 2 );
	}

	public Connection getConnection()
	{
		return connection;
	}
}
