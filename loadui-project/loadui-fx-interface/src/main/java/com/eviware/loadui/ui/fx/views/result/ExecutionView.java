package com.eviware.loadui.ui.fx.views.result;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.RectangleBuilder;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.Properties;

public class ExecutionView extends Region
{
	private final Execution execution;

	public ExecutionView( final Execution execution )
	{
		this.execution = execution;
		setStyle( "-fx-background-color: black, darkgrey; -fx-background-insets: 0, 2; -fx-background-radius: 5;" );

		Label executionLabel = LabelBuilder.create().build();
		executionLabel.textProperty().bind( Properties.forLabel( execution ) );

		final Task<Void> loadAndOpenExecution = new Task<Void>()
		{
			{
				updateMessage( "Loading execution: " + execution.getLabel() );
			}

			@Override
			protected Void call() throws Exception
			{
				execution.getTestEventCount();
				return null;
			}
		};
		loadAndOpenExecution.setOnSucceeded( new EventHandler<WorkerStateEvent>()
		{
			@Override
			public void handle( WorkerStateEvent workserStateEvent )
			{
				ExecutionView.this.fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, ExecutionView.this.execution ) );
			}
		} );

		getChildren().add(
				VBoxBuilder
						.create()
						.padding( new Insets( 5 ) )
						.spacing( 5 )
						.alignment( Pos.CENTER_RIGHT )
						.children(
								ButtonBuilder.create().text( "X" ).onAction( new EventHandler<ActionEvent>()
								{
									@Override
									public void handle( ActionEvent arg0 )
									{
										ExecutionView.this.execution.delete();
									}
								} ).build(),
								executionLabel,
								RectangleBuilder.create().width( 100 ).height( 75 ).fill( Color.LEMONCHIFFON )
										.onMouseClicked( new EventHandler<MouseEvent>()
										{
											@Override
											public void handle( MouseEvent event )
											{
												ExecutionView.this.fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING,
														loadAndOpenExecution ) );
											}
										} ).build() ).build() );
	}
}
