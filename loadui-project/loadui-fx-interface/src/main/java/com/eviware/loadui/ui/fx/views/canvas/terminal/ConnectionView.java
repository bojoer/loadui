package com.eviware.loadui.ui.fx.views.canvas.terminal;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.CubicCurveBuilder;

import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.input.Selectable;
import com.eviware.loadui.ui.fx.views.canvas.CanvasObjectView;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ConnectionView extends Group
{
	private final Connection connection;
	private final OutputTerminalView outputTerminalView;
	private final InputTerminalView inputTerminalView;

	private final CubicCurve outline = CubicCurveBuilder.create().fill( null ).stroke( Color.BLACK ).strokeWidth( 8 )
			.build();
	private final CubicCurve wire = CubicCurveBuilder.create().fill( null ).strokeWidth( 6 ).build();

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

		wire.strokeProperty().bind(
				Bindings.when( Selectable.installSelectable( this ).selectedProperty() ).then( Color.BLUE )
						.otherwise( Color.LIGHTGRAY ) );

		getChildren().addAll( outline, wire );

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

		outline.setStartX( startX );
		outline.setStartY( startY );
		outline.setControlX1( startX );
		outline.setControlY1( startY + control );
		outline.setControlX2( endX );
		outline.setControlY2( endY - control );
		outline.setEndX( endX );
		outline.setEndY( endY );

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
