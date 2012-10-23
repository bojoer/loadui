package com.eviware.loadui.ui.fx.views.canvas;

import javax.annotation.Nonnull;
import javax.swing.plaf.basic.BasicBorders.ToggleButtonBorder;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.util.TestExecutionUtils;

public class PlayButton extends StackPane
{
	private final ToggleButton toggleButton = ToggleButtonBuilder.create().build();
	private final CanvasItem canvas;
	private final BooleanProperty playingProperty = new SimpleBooleanProperty();

	protected final ChangeListener<Boolean> playCanvas = new ChangeListener<Boolean>()
	{
		@Override
		public void changed( ObservableValue<? extends Boolean> observable, Boolean wasPlaying, Boolean isPlaying )
		{
			if( isPlaying && !canvas.isRunning() )
			{
				canvas.triggerAction( CanvasItem.COUNTER_RESET_ACTION );
				canvas.triggerAction( CanvasItem.START_ACTION );
				TestExecutionUtils.startCanvas( canvas );
			}
			else if( canvas.isRunning() )
			{
				canvas.triggerAction( CanvasItem.STOP_ACTION );
				TestExecutionUtils.stopCanvas( canvas );
			}
		}
	};

	public PlayButton( @Nonnull final CanvasItem canvas )
	{
		this.canvas = canvas;

		playingProperty.addListener( playCanvas );
		playingProperty.bindBidirectional( toggleButton.selectedProperty() );
		ProgressIndicator playSpinner = new ProgressIndicator();
		playSpinner.visibleProperty().bind( toggleButton.selectedProperty() );
		toggleButton.textProperty().bind(
				Bindings.when( toggleButton.selectedProperty() ).then( "\u25FC" ).otherwise( "\u25B6" ) );
		toggleButton.setId( "play-button" );

		TestExecutionUtils.testRunner.registerTask( new TestExecutionTask()
		{
			@Override
			public void invoke( final TestExecution execution, final Phase phase )
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						if( canvas instanceof SceneItem && ( ( SceneItem )canvas ).isAffectedByExecutionTask( execution ) )
							playingProperty.set( phase == Phase.PRE_START );
					}
				} );
			}
		}, Phase.PRE_START, Phase.POST_STOP );

		getChildren().setAll( playSpinner, toggleButton );
	}
}