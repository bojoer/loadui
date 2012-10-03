package com.eviware.loadui.ui.fx.views.canvas;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.model.CanvasItem;

public abstract class PlaybackPanel<T extends CounterDisplay> extends HBox
{
	protected CanvasItem canvas;

	public final static String TIME_LABEL = "Time";
	public final static String REQUESTS_LABEL = "Requests";
	public final static String FAILURES_LABEL = "Failures";

	protected final T time;
	protected final T requests;
	protected final T failures;

	protected PlayButton playButton = new PlayButton();

	public PlaybackPanel()
	{
		time = timeCounter();
		requests = timeRequests();
		failures = timeFailures();
	}

	protected abstract T timeCounter();

	protected abstract T timeRequests();

	protected abstract T timeFailures();

	protected final EventHandler<ActionEvent> resetCounters = new EventHandler<ActionEvent>()
	{
		@Override
		public void handle( ActionEvent e )
		{
			canvas.triggerAction( CounterHolder.COUNTER_RESET_ACTION );
		}
	};

	protected final EventHandler<ActionEvent> openLimitsDialog = new EventHandler<ActionEvent>()
	{
		@Override
		public void handle( ActionEvent e )
		{
			new LimitsDialog( PlaybackPanel.this, canvas ).show();
		}
	};

	public void setCanvas( @Nonnull final CanvasItem canvas )
	{
		this.canvas = canvas;
		playButton.setCanvas( canvas );
		Timeline updateDisplays = new Timeline( new KeyFrame( Duration.millis( 500 ), new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				time.setValue( canvas.getCounter( CanvasItem.TIMER_COUNTER ).get() );
				requests.setValue( canvas.getCounter( CanvasItem.REQUEST_COUNTER ).get() );
				failures.setValue( canvas.getCounter( CanvasItem.FAILURE_COUNTER ).get() );
			}
		} ) );
		updateDisplays.setCycleCount( Timeline.INDEFINITE );
		updateDisplays.play();
	}

	protected Button resetButton()
	{
		return ButtonBuilder.create().text( "Reset" ).style( "-fx-font-size: 10px;" ).onAction( resetCounters ).build();
	}

	protected static Separator separator()
	{
		return new Separator( Orientation.VERTICAL );
	}

	protected Image image( String name )
	{
		return new Image( getClass().getResourceAsStream( name ) );
	}
}
