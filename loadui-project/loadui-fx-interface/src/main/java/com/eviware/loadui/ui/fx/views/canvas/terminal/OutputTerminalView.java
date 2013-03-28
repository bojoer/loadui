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
package com.eviware.loadui.ui.fx.views.canvas.terminal;

import javafx.event.EventHandler;
import javafx.fxml.FXML;

import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;

public class OutputTerminalView extends TerminalView
{
	public OutputTerminalView( OutputTerminal terminal )
	{
		super( terminal );

		getStyleClass().add( "output-terminal" );
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
				//Only accept if we do not already have a connection from this OutputTerminal.
				if( event.getData() instanceof InputTerminal && getTerminal().getConnections().isEmpty() )
				{
					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
					{
						event.accept();
						event.consume();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						InputTerminal inputTerminal = ( InputTerminal )event.getData();
						getTerminal().connectTo( inputTerminal );
						event.consume();
					}
				}
			}
		} );
	}

	@Override
	public OutputTerminal getTerminal()
	{
		return ( OutputTerminal )super.getTerminal();
	}
}
