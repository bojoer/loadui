package com.eviware.loadui.ui.fx.views.canvas;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.SettingsDialog;
import com.eviware.loadui.ui.fx.control.fields.ValidatableLongField;

public class LimitsDialog extends ConfirmationDialog
{
	public LimitsDialog( @Nonnull Node owner, @Nonnull final CanvasItem canvas )
	{
		super( owner, "Limits", "Save" );

		VBox vBox = new VBox( SettingsDialog.VERTICAL_SPACING );

		final ValidatableLongField timeField = new ValidatableLongField( ValidatableLongField.EMPTY_TO_NEGATIVE_ONE,
				longToString( canvas.getLimit( CanvasItem.TIMER_COUNTER ) ) );
		final ValidatableLongField requestField = new ValidatableLongField( ValidatableLongField.EMPTY_TO_NEGATIVE_ONE,
				longToString( canvas.getLimit( CanvasItem.REQUEST_COUNTER ) ) );
		final ValidatableLongField failureField = new ValidatableLongField( ValidatableLongField.EMPTY_TO_NEGATIVE_ONE,
				longToString( canvas.getLimit( CanvasItem.FAILURE_COUNTER ) ) );

		vBox.getChildren().addAll( new Label( "Time limit :" ), timeField, new Label( "Request limit :" ), requestField,
				new Label( "Failure limit :" ), failureField );

		getItems().add( vBox );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent arg0 )
			{
				if( timeField.validate() && requestField.validate() && failureField.validate() )
				{
					canvas.setLimit( CanvasItem.TIMER_COUNTER, timeField.getValue() );
					canvas.setLimit( CanvasItem.REQUEST_COUNTER, timeField.getValue() );
					canvas.setLimit( CanvasItem.FAILURE_COUNTER, timeField.getValue() );
					close();
				}
			}
		} );
	}

	private static String longToString( long value )
	{
		if( value == -1 )
			return "";
		return Long.toString( value );
	}
}
