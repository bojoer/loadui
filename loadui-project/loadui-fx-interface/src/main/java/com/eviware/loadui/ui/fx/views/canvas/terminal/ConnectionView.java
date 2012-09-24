package com.eviware.loadui.ui.fx.views.canvas.terminal;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.CubicCurveBuilder;

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
	private final CubicCurve wire = CubicCurveBuilder.create().fill( Color.TRANSPARENT ).stroke( Color.LIGHTGRAY )
			.strokeWidth( 8 ).build();

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

		double startX = ( startBounds.getMaxX() + startBounds.getMinX() ) / 2;
		double startY = ( startBounds.getMaxY() + startBounds.getMinY() ) / 2;
		double endX = ( endBounds.getMaxX() + endBounds.getMinX() ) / 2;
		double endY = ( endBounds.getMaxY() + endBounds.getMinY() ) / 2;
		double control = Math.min( Math.sqrt( Math.pow( startX - endX, 2 ) + Math.pow( startY - endY, 2 ) ), 200 );

		wire.setStartX( startX );
		wire.setStartY( startY );
		wire.setControlX1( startX );
		wire.setControlY1( startY + control );
		wire.setControlX2( endX );
		wire.setControlY2( endY - control );
		wire.setEndX( endX );
		wire.setEndY( endY );
	}

	public Connection getConnection()
	{
		return connection;
	}
}
