package com.eviware.loadui.ui.fx.control;

import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;

import javax.annotation.Nonnull;

import com.sun.javafx.PlatformUtil;

public class ConfirmationDialog extends ButtonDialog
{
	private final Button confirmButton;
	private final Button cancelButton;

	public ConfirmationDialog( @Nonnull final Node owner, @Nonnull String header, @Nonnull String actionButtonLabel )
	{
		super( owner, header );

		confirmButton = ButtonBuilder.create().alignment( Pos.BOTTOM_RIGHT ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				close();
			}
		} ).text( actionButtonLabel ).defaultButton( true ).build();

		cancelButton = ButtonBuilder.create().alignment( Pos.BOTTOM_RIGHT ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				close();
			}
		} ).text( "Cancel" ).cancelButton( true ).build();

		if( PlatformUtil.isMac() )
			getButtons().setAll( cancelButton, confirmButton );
		else
			getButtons().setAll( confirmButton, cancelButton );
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
