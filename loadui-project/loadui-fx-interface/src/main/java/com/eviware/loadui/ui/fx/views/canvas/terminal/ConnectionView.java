package com.eviware.loadui.ui.fx.views.canvas.terminal;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GlowBuilder;
import javafx.scene.paint.Color;

import com.eviware.loadui.api.events.TerminalConnectionEvent;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.input.Selectable;
import com.eviware.loadui.ui.fx.views.canvas.CanvasObjectView;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ConnectionView extends Wire implements Deletable
{
	private static final Effect SELECTED_EFFECT = GlowBuilder.create().build();
	private final Connection connection;
	private final OutputTerminalView outputTerminalView;
	private final InputTerminalView inputTerminalView;
	private final ReadOnlyBooleanProperty selectedProperty;

	public ConnectionView( final Connection connection, final CanvasObjectView outputComponentView,
			final CanvasObjectView inputComponentView )
	{
		super();

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

		selectedProperty = Selectable.installSelectable( this ).selectedProperty();
		fillProperty().bind( Bindings.when( selectedProperty ).then( Color.BLUE ).otherwise( Color.LIGHTGRAY ) );
		effectProperty().bind( Bindings.when( selectedProperty ).then( SELECTED_EFFECT ).otherwise( ( Effect )null ) );
		selectedProperty.addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> property, Boolean oldSelected, Boolean selected )
			{
				if( selected )
				{
					toFront();
				}
			}
		} );

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

		updatePosition( startX, startY, endX, endY );
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

	public ReadOnlyBooleanProperty selectedProperty()
	{
		return selectedProperty;
	}

	public boolean isSelected()
	{
		return selectedProperty.get();
	}

	@Override
	public void delete()
	{
		connection.disconnect();
	}
}
