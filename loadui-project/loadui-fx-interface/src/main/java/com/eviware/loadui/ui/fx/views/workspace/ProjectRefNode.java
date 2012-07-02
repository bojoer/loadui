package com.eviware.loadui.ui.fx.views.workspace;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.RectangleBuilder;

import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.Properties;

public class ProjectRefNode extends Region
{
	private final ProjectRef projectRef;

	public ProjectRefNode( final ProjectRef projectRef )
	{
		this.projectRef = projectRef;
		getStyleClass().setAll( "project-ref-node" );
		setStyle( "-fx-background-color: black, darkgrey; -fx-background-insets: 0, 2; -fx-background-radius: 5;" );

		Label projectRefLabel = LabelBuilder.create().build();
		projectRefLabel.textProperty().bind( Properties.forLabel( projectRef ) );

		getChildren().add(
				VBoxBuilder
						.create()
						.padding( new Insets( 5 ) )
						.spacing( 5 )
						.children(
								ButtonBuilder.create().text( "X" ).onAction( new EventHandler<ActionEvent>()
						{
							@Override
							public void handle( ActionEvent arg0 )
							{
								projectRef.delete( false );
							}
								} ).build(),
								projectRefLabel,
								RectangleBuilder.create().width( 100 ).height( 75 ).fill( Color.PINK )
										.onMouseClicked( new EventHandler<MouseEvent>()
										{
											@Override
											public void handle( MouseEvent event )
											{
												fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, projectRef ) );
	}
										} ).build() ).build() );
	}
}
