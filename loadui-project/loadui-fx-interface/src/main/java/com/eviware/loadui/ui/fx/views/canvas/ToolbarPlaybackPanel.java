package com.eviware.loadui.ui.fx.views.canvas;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.ui.fx.views.canvas.CounterDisplay.Formatting;

public abstract class ToolbarPlaybackPanel<C extends CanvasItem> extends PlaybackPanel<ToolbarCounterDisplay, C>
{
	protected Button limitsButton()
	{
		return ButtonBuilder.create().id( "set-limits" ).text( "Set Limits\u2026" ).style( "-fx-font-size: 10px;" )
				.onAction( openLimitsDialog ).build();
	}

	public ToolbarPlaybackPanel( final C canvas )
	{
		super( canvas );
		time.setLimit( canvas.getLimit( CanvasItem.TIMER_COUNTER ) );
		requests.setLimit( canvas.getLimit( CanvasItem.REQUEST_COUNTER ) );
		failures.setLimit( canvas.getLimit( CanvasItem.FAILURE_COUNTER ) );

		canvas.addEventListener( BaseEvent.class, new com.eviware.loadui.api.events.EventHandler<BaseEvent>()
		{
			@Override
			public void handleEvent( BaseEvent event )
			{
				time.setLimit( canvas.getLimit( TIME_LABEL ) );
				requests.setLimit( canvas.getLimit( REQUESTS_LABEL ) );
				failures.setLimit( canvas.getLimit( FAILURES_LABEL ) );
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
