package com.eviware.loadui.ui.fx.views.about;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PopupControl;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlurBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class AboutDialog extends PopupControl
{
	private final Node owner;

	public AboutDialog( Node owner )
	{
		this.owner = owner;

		setAutoHide( true );

		bridge.getChildren().setAll( FXMLUtils.load( AboutDialog.class, new Callable<Controller>()
		{
			@Override
			public Controller call() throws Exception
			{
				return new Controller();
			}
		} ) );

		Scene ownerScene = owner.getScene();
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

	private void blurParentWindow()
	{
		final Parent root = owner.getScene().getRoot();
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

	public class Controller implements Initializable
	{
		@FXML
		private ImageView logo;

		@FXML
		private ImageView smartbear;

		@Override
		public void initialize( URL arg0, ResourceBundle arg1 )
		{
			logo.setImage( new Image( "res/about-logo.png" ) );
			logo.setTranslateY( -80 );
		}

		public void loaduiSite()
		{
			System.out.println( "www.loadui.org" );
		}

		public void smartbearSite()
		{
			System.out.println( "www.smartbear.com" );
		}
	}
}
