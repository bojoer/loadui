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
package com.eviware.loadui.ui.fx.views.canvas;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.ui.fx.views.canvas.CounterDisplay.Formatting;

public abstract class ToolbarPlaybackPanel<C extends CanvasItem> extends PlaybackPanel<ToolbarCounterDisplay, C>
{
	protected Button limitsButton()
	{
		return ButtonBuilder.create().id( "set-limits" ).text( "Set limits" )
				.style( "-fx-font-size: 10px; -fx-translate-x: -3;" ).onAction( openLimitsDialog ).build();
	}

	public ToolbarPlaybackPanel( final C canvas )
	{
		super( canvas );
		time.setLimit( canvas.getLimit( CanvasItem.TIMER_COUNTER ) );
		requests.setLimit( canvas.getLimit( CanvasItem.REQUEST_COUNTER ) );
		failures.setLimit( canvas.getLimit( CanvasItem.FAILURE_COUNTER ) );

		setSpacing( 9 );

		canvas.addEventListener( BaseEvent.class, new com.eviware.loadui.api.events.EventHandler<BaseEvent>()
		{
			@Override
			public void handleEvent( BaseEvent event )
			{
				Platform.runLater( new Runnable()
				{
					public void run()
					{
						time.setLimit( canvas.getLimit( TIME_LABEL ) );
						requests.setLimit( canvas.getLimit( CanvasItem.REQUEST_COUNTER ) );
						failures.setLimit( canvas.getLimit( FAILURES_LABEL ) );
					}
				} );
			}
		} );
	}

	@Override
	protected ToolbarCounterDisplay timeCounter()
	{
		return new ToolbarCounterDisplay( TIME_LABEL, Formatting.TIME );
	}

	@Override
	protected ToolbarCounterDisplay timeRequests()
	{

		return new ToolbarCounterDisplay( REQUESTS_LABEL );
	}

	@Override
	protected ToolbarCounterDisplay timeFailures()
	{
		return new ToolbarCounterDisplay( FAILURES_LABEL );
	}
}
