package com.eviware.loadui.ui.fx.control;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.GaussianBlurBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.annotation.Nonnull;

public class Dialog extends Stage
{
	private static final GaussianBlur BLUR = GaussianBlurBuilder.create().radius( 8 ).build();
	private final Node owner;
	private final Pane rootPane;

	public Dialog( @Nonnull final Node owner )
	{
		this.owner = owner;
		final Scene ownerScene = owner.getScene();

		rootPane = VBoxBuilder.create().styleClass( "dialog" ).minWidth( 300 ).build();

		Scene scene = new Scene( rootPane );
		Bindings.bindContent( scene.getStylesheets(), ownerScene.getStylesheets() );
		setScene( scene );

		setResizable( false );
		initStyle( StageStyle.UTILITY );
		initModality( Modality.APPLICATION_MODAL );

		Window parentWindow = ownerScene.getWindow();
		final double x = parentWindow.getX() + parentWindow.getWidth() / 2;
		final double y = parentWindow.getY() + parentWindow.getHeight() / 2;

		// Set a good estimated position before the dialog is shown to avoid flickering.
		setY( y - getScene().getRoot().prefHeight( -1 ) / 2 );
		setX( x - getScene().getRoot().prefWidth( -1 ) / 2 );

		addEventHandler( WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent arg0 )
			{
				blurParentWindow();

				setX( x - getWidth() / 2 );
				setY( y - getHeight() / 2 );
			}
		} );
	}

	public ObservableList<Node> getItems()
	{
		return rootPane.getChildren();
	}

	private void blurParentWindow()
	{
		final Parent root = owner.getScene().getRoot();
		root.setEffect( BLUR );
		addEventHandler( WindowEvent.WINDOW_HIDING, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent arg0 )
			{
				if( root.getEffect() == BLUR )
				{
					root.setEffect( null );
				}
			}
		} );
	}
}
