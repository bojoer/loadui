package com.eviware.loadui.ui.fx.views.about;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlurBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.google.common.collect.ImmutableMap;

public class AboutDialog extends PopupControl
{
	private final Node owner;

	public AboutDialog( Node owner )
	{
		this.owner = owner;

		setAutoHide( true );

		bridge.getChildren()
				.setAll(
						FXMLUtils.load( AboutDialog.class, new Callable<Controller>()
						{
							@Override
							public Controller call() throws Exception
							{
								return new Controller();
							}
						}, ImmutableMap.of( "name", System.getProperty( LoadUI.NAME ), "version", LoadUI.VERSION,
								"buildDate", System.getProperty( LoadUI.BUILD_DATE ), "buildVersion",
								System.getProperty( LoadUI.BUILD_NUMBER ) ) ) );

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
		private Label title;

		@FXML
		private Label buildVersion;

		@FXML
		private Label buildDate;

		@Override
		public void initialize( URL arg0, ResourceBundle arg1 )
		{
			logo.setImage( new Image( "res/about-logo.png" ) );

			title.setText( String.format( "%s Version %s", System.getProperty( LoadUI.NAME, "loadUI" ), LoadUI.VERSION ) );
			buildVersion.setText( String.format( "Build version: %s",
					System.getProperty( LoadUI.BUILD_NUMBER, "[internal]" ) ) );
			buildDate.setText( String.format( "Build version: %s", System.getProperty( LoadUI.BUILD_DATE, "unknown" ) ) );
		}

		public void loaduiSite()
		{
			try
			{
				Desktop.getDesktop().browse( new URI( "http://www.loadui.org" ) );
			}
			catch( IOException | URISyntaxException e )
			{
				e.printStackTrace();
			}
		}

		public void smartbearSite()
		{
			try
			{
				Desktop.getDesktop().browse( new URI( "http://www.smartbear.com" ) );
			}
			catch( IOException | URISyntaxException e )
			{
				e.printStackTrace();
			}
		}
	}
}
