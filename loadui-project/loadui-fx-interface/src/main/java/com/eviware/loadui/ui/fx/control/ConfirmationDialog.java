package com.eviware.loadui.ui.fx.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorBuilder;
import javafx.scene.layout.HBox;

import javax.annotation.Nonnull;

import com.sun.javafx.PlatformUtil;

public class ConfirmationDialog extends ButtonDialog
{
	private final Button confirmButton;
	private final Button cancelButton;

	public ConfirmationDialog( @Nonnull final Node owner, @Nonnull String header, @Nonnull String actionButtonLabel )
	{
		this( owner, header, actionButtonLabel, false );
		addStyleClass( "confirmation-dialog" );
	}

	public ConfirmationDialog( @Nonnull final Node owner, @Nonnull String header, @Nonnull String actionButtonLabel,
			boolean separateButtons )
	{
		super( owner, header );

		confirmButton = ButtonBuilder.create().text( actionButtonLabel ).id( "default" ).defaultButton( true )
				.alignment( Pos.BOTTOM_RIGHT ).onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						close();
					}
				} ).build();

		cancelButton = ButtonBuilder.create().text( "Cancel" ).id( "cancel" ).cancelButton( true )
				.alignment( Pos.BOTTOM_RIGHT ).onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						close();
					}
				} ).build();

		if( separateButtons )
		{
			Separator buttonSeparator = SeparatorBuilder.create().style( "visibility: hidden;" ).maxWidth( 4 )
					.minWidth( 4 ).build();
			HBox.setHgrow( buttonSeparator, javafx.scene.layout.Priority.ALWAYS );
			getButtons().setAll( cancelButton, buttonSeparator, confirmButton );
		}
		else if( PlatformUtil.isMac() )
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

	public ObjectProperty<EventHandler<ActionEvent>> onCancelProperty()
	{
		return cancelButton.onActionProperty();
	}

	public EventHandler<ActionEvent> getOnCancel()
	{
		return cancelButton.onActionProperty().get();
	}

	public void setOnCancel( EventHandler<ActionEvent> value )
	{
		cancelButton.onActionProperty().set( value );
	}

	public BooleanProperty confirmDisableProperty()
	{
		return confirmButton.disableProperty();
	}

	public boolean isConfirmDisable()
	{
		return confirmButton.isDisable();
	}

	public void setConfirmDisable( boolean disable )
	{
		confirmButton.setDisable( disable );
	}

	public StringProperty confirmationTextProperty()
	{
		return confirmButton.textProperty();
	}
}
