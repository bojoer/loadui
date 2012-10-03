package com.eviware.loadui.ui.fx.views.canvas;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.Dialog;
import com.eviware.loadui.ui.fx.control.SettingsDialog;

public class LimitsDialog extends ConfirmationDialog
{
	public LimitsDialog( @Nonnull Node owner, @Nonnull final CanvasItem canvas )
	{
		super( owner, "Limits", "Save" );

		VBox vBox = new VBox( SettingsDialog.VERTICAL_SPACING );

		final TextField timeField = new TextField( longToString( canvas.getLimit( CanvasItem.TIMER_COUNTER ) ) );
		final TextField requestField = new TextField( longToString( canvas.getLimit( CanvasItem.REQUEST_COUNTER ) ) );
		final TextField failureField = new TextField( longToString( canvas.getLimit( CanvasItem.FAILURE_COUNTER ) ) );

		vBox.getChildren().addAll( new Label( "Time limit :" ), timeField, new Label( "Request limit :" ), requestField,
				new Label( "Failure limit :" ), failureField );

		getItems().add( vBox );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent arg0 )
			{
				canvas.setLimit( CanvasItem.TIMER_COUNTER, fieldToLong( timeField ) );
				canvas.setLimit( CanvasItem.REQUEST_COUNTER, fieldToLong( requestField ) );
				canvas.setLimit( CanvasItem.FAILURE_COUNTER, fieldToLong( failureField ) );
				close();
			}
		} );
	}

	private static String longToString( long value )
	{
		if( value == -1 )
			return "";
		return Long.toString( value );
	}

	private static long fieldToLong( TextField textField )
	{
		if( textField.getText().isEmpty() )
			return -1;
		return Dialog.getLong( textField );
	}
}
