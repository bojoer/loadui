package com.eviware.loadui.ui.fx.control;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Effect;
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
	private final Scene parentScene;
	private final Pane rootPane;

	public Dialog( @Nonnull final Scene parentScene )
	{
		this.parentScene = parentScene;

		rootPane = VBoxBuilder.create().spacing( 6 ).padding( new Insets( 10, 24, 20, 24 ) ).minWidth( 300 ).build();

		setScene( new Scene( rootPane ) );

		//		setResizable( false );
		initStyle( StageStyle.UTILITY );
		initModality( Modality.APPLICATION_MODAL );

		blurParentWindow();

		Window parentWindow = parentScene.getWindow();
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
