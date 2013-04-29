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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressIndicatorBuilder;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;

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
	private final ToggleButton toggleButton = ToggleButtonBuilder
			.create()
			.styleClass( "play-button" )
			.graphic(
					HBoxBuilder
							.create()
							.children( RegionBuilder.create().styleClass( "graphic" ).build(),
									RegionBuilder.create().styleClass( "secondary-graphic" ).build() ).build() ).build();
	
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
			log.debug( "Play Button state changed, isPlaying? " + isPlaying + ", isCanvasRunning? " + canvas.isRunning() );
			if( isPlaying && !canvas.isRunning() )
			{
				canvas.triggerAction( CanvasItem.COUNTER_RESET_ACTION );
				canvas.triggerAction( CanvasItem.START_ACTION );
				TestExecutionUtils.startCanvas( canvas );
			}
			else if( canvas.isRunning() && !isPlaying )
			{
				canvas.triggerAction( CanvasItem.STOP_ACTION );
				TestExecutionUtils.stopCanvas( canvas );
			}
		}
	};

	public PlayButton( @Nonnull final CanvasItem canvas )
	{
		this.canvas = canvas;

		maxHeight( 27 );
		maxWidth( 27 );

		playingProperty.addListener( playCanvas );
		playingProperty.bindBidirectional( toggleButton.selectedProperty() );

		Circle border = CircleBuilder.create().styleClass( "play-button-border" ).radius( 14 ).build();
		Region inner = RegionBuilder.create().styleClass( "inner-spinner-overlay" ).build();
		Region outer = RegionBuilder.create().styleClass( "outer-spinner-overlay" ).build();
		ProgressIndicator indicator = ProgressIndicatorBuilder.create().build();

		inner.visibleProperty().bind( toggleButton.selectedProperty() );
		outer.visibleProperty().bind( toggleButton.selectedProperty() );
		indicator.visibleProperty().bind( toggleButton.selectedProperty() );
		border.visibleProperty().bind( toggleButton.selectedProperty().not() );

		TestExecutionUtils.testRunner.registerTask( executionTask, Phase.PRE_START, Phase.POST_STOP );
		getChildren().setAll( outer, indicator, inner, border, toggleButton );

		setMaxSize( 27, 27 );
	}
}
