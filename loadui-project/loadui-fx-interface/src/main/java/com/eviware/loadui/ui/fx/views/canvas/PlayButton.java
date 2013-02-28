package com.eviware.loadui.ui.fx.views.canvas;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.util.execution.TestExecutionUtils;

public class PlayButton extends StackPane
{
	private final ToggleButton toggleButton = ToggleButtonBuilder.create().styleClass( "styleable-graphic", "play-button" ).build();
	private final CanvasItem canvas;
	private final BooleanProperty playingProperty = new SimpleBooleanProperty();

	protected static final Logger log = LoggerFactory.getLogger( PlayButton.class );

	private final TestExecutionTask executionTask = new TestExecutionTask()
	{
		@Override
		public void invoke( final TestExecution execution, final Phase phase )
		{
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					CanvasItem startedCanvas = execution.getCanvas();

					if( canvas instanceof SceneItem )
					{
						if( ( ( SceneItem )canvas ).isAffectedByExecutionTask( execution ) )
						{
							playingProperty.set( phase == Phase.PRE_START );
						}
					}
					else if( canvas instanceof ProjectItem )
					{
						if( ( ( ProjectItem )canvas ).getCanvas() == startedCanvas )
							playingProperty.set( phase == Phase.PRE_START );
					}
					else
					{
						log.warn( "Unsupported CanvasItem: " + canvas.toString() );
					}
				}
			} );
		}
	};

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

		TestExecutionUtils.testRunner.registerTask( executionTask, Phase.PRE_START, Phase.POST_STOP );

		getChildren().setAll(RegionBuilder.create().styleClass( "outer-spinner-overlay" ).build(), playSpinner, RegionBuilder.create().styleClass("inner-spinner-overlay").build(),toggleButton );
	}
}