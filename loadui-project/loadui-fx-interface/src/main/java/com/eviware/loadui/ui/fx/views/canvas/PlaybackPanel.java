package com.eviware.loadui.ui.fx.views.canvas;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.model.CanvasItem;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.util.Duration;

public abstract class PlaybackPanel extends HBox
{
	protected CanvasItem canvas;

	public final static String TIME = "Time";
	public final static String REQUESTS = "Requests";
	public final static String FAILURES = "Failures";

	protected final CounterDisplay time;
	protected final CounterDisplay requests;
	protected final CounterDisplay failures;

	public PlaybackPanel()
	{
		time = timeCounter();
		requests = timeRequests();
		failures = timeFailures();
	}

	protected abstract CounterDisplay timeCounter();

	protected abstract CounterDisplay timeRequests();

	protected abstract CounterDisplay timeFailures();

	protected final ChangeListener<Boolean> playCanvas = new ChangeListener<Boolean>()
	{
		@Override
		public void changed( ObservableValue<? extends Boolean> observable, Boolean wasSelected, Boolean isSelected )
		{
			if( isSelected )
			{
				canvas.triggerAction( CounterHolder.COUNTER_RESET_ACTION );
				canvas.triggerAction( CanvasItem.START_ACTION );
			}
			else
			{
				canvas.triggerAction( CanvasItem.STOP_ACTION );
			}
		}
	};

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
			LimitsDialog.instanceOf( PlaybackPanel.this ).show();
		}
	};

	public void setCanvas( @Nonnull final CanvasItem canvas )
	{
		this.canvas = canvas;
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

	protected Button limitsButton()
	{
		return ButtonBuilder.create().text( "Limits\u2026" ).style( "-fx-font-size: 10px;" ).onAction( openLimitsDialog )
				.build();
	}

	protected static Separator separator()
	{
		return new Separator( Orientation.VERTICAL );
	}

	protected Image image( String name )
	{
		return new Image( getClass().getResourceAsStream( name ) );
	}

	protected StackPane playStack()
	{
		ToggleButton playButton = new ToggleButton();
		playButton.selectedProperty().addListener( playCanvas );
		ProgressIndicator playSpinner = new ProgressIndicator();
		playSpinner.visibleProperty().bind( playButton.selectedProperty() );
		playButton.textProperty().bind(
				Bindings.when( playButton.selectedProperty() ).then( "\u25FC" ).otherwise( "\u25B6" ) );
		StackPane playStack = StackPaneBuilder.create().children( playSpinner, playButton ).build();
		return playStack;
	}
}
