package com.eviware.loadui.ui.fx.control;

import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;

public class ErrorDialog extends ButtonDialog
{
	private final Button confirmButton;

	public ErrorDialog( Node owner, String header, String messageFormat, Object... args )
	{
		super( owner, header );

		confirmButton = ButtonBuilder.create().alignment( Pos.BOTTOM_RIGHT ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				close();
			}
		} ).text( "OK" ).defaultButton( true ).build();

		getItems().setAll( new Label( String.format( messageFormat, args ) ) );
		getButtons().setAll( confirmButton );
	}

	public ObjectProperty<EventHandler<ActionEvent>> onConfirmProperty()
	{
		return confirmButton.onActionProperty();
	}

	public EventHandler<ActionEvent> getOnConfirm()
	{
		return confirmButton.onActionProperty().get();
	}

	public void setOnConfirm( EventHandler<ActionEvent> value )
	{
		confirmButton.onActionProperty().set( value );
	}
}