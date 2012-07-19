package com.eviware.loadui.ui.fx.control;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.LabelBuilder;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlurBuilder;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.annotation.Nonnull;

public class Dialog extends Stage
{
	final Scene parentScene;

	public Dialog( @Nonnull final Scene parentScene )
	{
		this.parentScene = parentScene;

		Parent rootParent = VBoxBuilder
				.create()
				.spacing( 25 )
				.padding( new Insets( 20, 75, 20, 75 ) )
				.alignment( Pos.CENTER )
				.children( LabelBuilder.create().text( "A dialog window" ).build(),
						ButtonBuilder.create().onAction( new EventHandler<ActionEvent>()
						{
							@Override
							public void handle( ActionEvent arg0 )
							{
								close();
							}
						} ).text( "Ok" ).build() ).build();

		setScene( new Scene( rootParent ) );

		setResizable( false );
		initStyle( StageStyle.UTILITY );
		initModality( Modality.APPLICATION_MODAL );

		blurParentWindow();

		Window parentWindow = parentScene.getWindow();
		final double x = parentWindow.getX() + parentWindow.getWidth() / 2;
		final double y = parentWindow.getY() + parentWindow.getHeight() / 2;

		addEventHandler( WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent arg0 )
			{
				setX( x - getWidth() / 2 );
				setY( y - getHeight() / 2 );
			}
		} );

	}

	private void blurParentWindow()
	{
		final Parent root = parentScene.getRoot();
		final Effect effect = root.getEffect();
		root.setEffect( GaussianBlurBuilder.create().radius( 8 ).build() );
		setOnHidden( new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent arg0 )
			{
				root.setEffect( effect );
			}
		} );
	}
}
