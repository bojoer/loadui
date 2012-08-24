package com.eviware.loadui.ui.fx.views.canvas;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.ui.fx.util.Properties;

public class ComponentView extends CanvasObjectView
{
	private final ComponentItem component;

	public ComponentView( ComponentItem component )
	{
		super( component );
		this.component = component;

		setPadding( new Insets( 5 ) );
		setStyle( "-fx-background-color: black, yellowgreen; -fx-background-insets: 0, 1; -fx-padding: 5;" );

		Label label = new Label();
		label.setId( "label" );
		label.textProperty().bind( Properties.forLabel( component ) );

		StackPane.setAlignment( label, Pos.TOP_LEFT );

		Button button = ButtonBuilder.create().text( "X" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				ComponentView.this.component.delete();
			}
		} ).build();

		StackPane.setAlignment( button, Pos.TOP_RIGHT );

		getChildren().addAll( label, button );
	}

	public ComponentItem getComponent()
	{
		return component;
	}
}
