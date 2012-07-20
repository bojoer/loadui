package com.eviware.loadui.ui.fx.control;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javax.annotation.Nonnull;

import com.sun.javafx.PlatformUtil;

public class ConfirmationDialog extends Dialog
{
	private final Button confirmButton;
	private final Button cancelButton;
	private final Pane itemPane = VBoxBuilder.create().spacing( 6 ).build();

	public ConfirmationDialog( @Nonnull final Scene parentScene, @Nonnull String header,
			@Nonnull String actionButtonLabel )
	{
		super( parentScene );

		final Label headerLabel = LabelBuilder.create().font( Font.font( null, FontWeight.BOLD, 14 ) ).text( header )
				.build();

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

		HBox buttonRow = HBoxBuilder.create().padding( new Insets( 12, 0, 0, 0 ) ).spacing( 15 )
				.alignment( Pos.BOTTOM_RIGHT ).build();
		if( PlatformUtil.isMac() )
			buttonRow.getChildren().setAll( cancelButton, confirmButton );
		else
			buttonRow.getChildren().setAll( confirmButton, cancelButton );

		super.getItems().setAll( headerLabel, itemPane, buttonRow );
	}

	@Override
	public ObservableList<Node> getItems()
	{
		return itemPane.getChildren();
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
