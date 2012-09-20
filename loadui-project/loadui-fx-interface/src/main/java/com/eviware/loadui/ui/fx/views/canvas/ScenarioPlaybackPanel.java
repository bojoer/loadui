package com.eviware.loadui.ui.fx.views.canvas;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.util.Duration;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.model.CanvasItem;

public class ScenarioPlaybackPanel extends HBox
{
	protected CanvasItem canvas;

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

	protected final ScenarioCounterDisplay time;

	protected final ScenarioCounterDisplay requests;

	protected final ScenarioCounterDisplay failures;

	public ScenarioPlaybackPanel()
	{
		setStyle( "-fx-spacing: 8; -fx-background-color: #8b8c8f; -fx-background-radius: 7;" );
		setMaxHeight( 28 );
		setMaxWidth( 245 );
		setAlignment( Pos.CENTER );

		ToggleButton playButton = new ToggleButton();
		playButton.selectedProperty().addListener( playCanvas );
		ProgressIndicator playSpinner = new ProgressIndicator();
		playSpinner.visibleProperty().bind( playButton.selectedProperty() );
		playButton.textProperty().bind(
				Bindings.when( playButton.selectedProperty() ).then( "\u25FC" ).otherwise( "\u25B6" ) );
		StackPane playStack = StackPaneBuilder.create().children( playSpinner, playButton ).build();

		time = new ScenarioCounterDisplay( "Time" );
		requests = new ScenarioCounterDisplay( "Requests" );
		failures = new ScenarioCounterDisplay( "Failures" );

		getChildren().setAll( playStack, separator(), time, separator(), requests, separator(), failures );
	}

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

	protected static Separator separator()
	{
		return new Separator( Orientation.VERTICAL );
	}
}
