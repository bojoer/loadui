package com.eviware.loadui.ui.fx.views.canvas.terminal;

import javafx.event.EventHandler;
import javafx.fxml.FXML;

import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;

public class InputTerminalView extends TerminalView
{
	public InputTerminalView( InputTerminal terminal )
	{
		super( terminal );

		getStyleClass().add( "input-terminal" );
	}

	@FXML
	@Override
	protected void initialize()
	{
		super.initialize();

		addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED && event.getData() instanceof OutputTerminal )
				{
					event.accept();
					event.consume();
				}
				else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
				{
					OutputTerminal outputTerminal = ( OutputTerminal )event.getData();
					outputTerminal.connectTo( getTerminal() );
					event.consume();
				}
			}
		} );
	}

	@Override
	public InputTerminal getTerminal()
	{
		return ( InputTerminal )super.getTerminal();
	}
}
