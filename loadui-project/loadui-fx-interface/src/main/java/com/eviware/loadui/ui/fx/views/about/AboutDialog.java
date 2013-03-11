package com.eviware.loadui.ui.fx.views.about;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.google.common.collect.ImmutableMap;

public class AboutDialog extends PopupControl
{
	@FXML
	private ImageView logo;

	@FXML
	private Label title;

	@FXML
	private Label buildVersion;

	@FXML
	private Label buildDate;

	private final Node owner;

	public AboutDialog( Node owner )
	{
		this.owner = owner;

		setAutoHide( true );

		//bridge.getChildren().setAll( this );

		FXMLLoader loader = new FXMLLoader( AboutDialog.class.getResource( AboutDialog.class.getSimpleName() + ".fxml" ) );
		loader.setClassLoader( FXMLUtils.class.getClassLoader() );
		loader.getNamespace().putAll(
				ImmutableMap.of( "name", System.getProperty( LoadUI.NAME ), "version", LoadUI.VERSION, "buildDate",
						System.getProperty( LoadUI.BUILD_DATE ), "buildVersion", System.getProperty( LoadUI.BUILD_NUMBER ) ) );
		loader.setController( this );

		try
		{
			bridge.getChildren().setAll( ( Parent )loader.load() );
		}
		catch( IOException exception )
		{
			throw new RuntimeException( exception );
		}

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

		logo.setImage( new Image( "res/about-logo.png" ) );

		title.setText( String.format( "%s Version %s", System.getProperty( LoadUI.NAME, "LoadUI" ), LoadUI.VERSION ) );
		buildVersion
				.setText( String.format( "Build version: %s", System.getProperty( LoadUI.BUILD_NUMBER, "[internal]" ) ) );
		buildDate.setText( String.format( "Build version: %s", System.getProperty( LoadUI.BUILD_DATE, "unknown" ) ) );
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

	public void loaduiSite()
	{
		UIUtils.openInExternalBrowser( "http://www.loadui.org" );
	}

	public void smartbearSite()
	{
		UIUtils.openInExternalBrowser( "http://www.smartbear.com" );
	}

}
