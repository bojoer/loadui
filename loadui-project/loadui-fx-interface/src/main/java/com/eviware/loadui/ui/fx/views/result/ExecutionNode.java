package com.eviware.loadui.ui.fx.views.result;

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

public class ExecutionNode extends Region
{
	private final Execution execution;

	public ExecutionNode( Execution execution )
	{
		this.execution = execution;
		setStyle( "-fx-background-color: black, darkgrey; -fx-background-insets: 0, 2; -fx-background-radius: 5;" );

		Label executionLabel = LabelBuilder.create().build();
		executionLabel.textProperty().bind( Properties.forLabel( execution ) );

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
										ExecutionNode.this.execution.delete();
									}
								} ).build(),
								executionLabel,
								RectangleBuilder.create().width( 100 ).height( 75 ).fill( Color.LEMONCHIFFON )
										.onMouseClicked( new EventHandler<MouseEvent>()
										{
											@Override
											public void handle( MouseEvent event )
											{
												ExecutionNode.this.fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN,
														ExecutionNode.this.execution ) );
											}
										} ).build() ).build() );
	}
}
