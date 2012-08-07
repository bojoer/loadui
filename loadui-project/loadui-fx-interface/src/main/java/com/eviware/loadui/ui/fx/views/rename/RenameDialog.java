package com.eviware.loadui.ui.fx.views.rename;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;

import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;

public class RenameDialog extends ConfirmationDialog
{
	public RenameDialog( final Labeled.Mutable labeled, Node owner )
	{
		super( owner, "Rename: " + labeled.getLabel(), "Rename" );

		Label newName = new Label( "New name" );
		final TextField newNameField = TextFieldBuilder.create().text( labeled.getLabel() ).build();

		getItems().setAll( newName, newNameField );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				close();
				labeled.setLabel( newNameField.getText() );
			}
		} );
	}
}
