/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.util.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.TextBuilder;
import javafx.stage.Stage;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
//import com.javafx.experiments.scenicview.ScenicView;

public class StyleTester extends Application
{

	protected Node createTestNode()
	{

		Image image = new Image("287.jpg", 500, 0, true, true);
		ImageView iview = new ImageView(image);

		iview.setPreserveRatio( true );
		return HBoxBuilder.create().children( iview, TextBuilder.create().text( "hello" ).build() ).build();
	}

	protected String[] getAdditionalStyleSheets() throws MalformedURLException
	{
		String[] styleSheets = { new File(
				"../loadui-fx-interface/src/main/resources/com/eviware/loadui/ui/fx/loadui-style.css" ).toURI().toURL()
				.toExternalForm() };
		return styleSheets;
  	}

	@Override
	public void start( final Stage primaryStage ) throws Exception
	{
		final StackPane panel = new StackPane();
		panel.getChildren().setAll( createTestNode() );

		final TextArea styleArea = TextAreaBuilder.create().build();

		final File styleSheet = File.createTempFile( "style", ".css" );

		styleArea.textProperty().addListener( new ChangeListener<String>()
		{
			@Override
			public void changed( ObservableValue<? extends String> arg0, String arg1, String arg2 )
			{
				try
				{
					Files.write( arg2, styleSheet, Charsets.UTF_8 );
					Platform.runLater( new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								styleArea.getScene().getStylesheets().setAll( styleSheet.toURI().toURL().toExternalForm() );
								styleArea.getScene().getStylesheets().addAll( getAdditionalStyleSheets() );

								System.out.println( "Updated style!" );
							}
							catch( MalformedURLException e )
							{
								e.printStackTrace();
							}
						}
					} );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		} );

		VBox.setVgrow( styleArea, Priority.ALWAYS );

		Button b = ButtonBuilder.create().text( "remove" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent arg0 )
			{
				createTestNode().getStyleClass().remove( "two" );

			}
		} ).build();

		primaryStage.setScene( SceneBuilder
				.create()
				.width( 1200 )
				.height( 480 )
				.root(
						SplitPaneBuilder
								.create()
								.items(
										panel,
										VBoxBuilder
												.create()
												.children(
														styleArea,
														b,
														ButtonBuilder.create().text( "Rebuild" )
																.onAction( new EventHandler<ActionEvent>()
																{
																	@Override
																	public void handle( ActionEvent arg0 )
																	{
																		panel.getChildren().setAll( createTestNode() );
																	}
																} ).build() ).build() ).build() ).build() );

		primaryStage.getScene().getStylesheets().setAll( getAdditionalStyleSheets() );

		primaryStage.show();

		//		final Wizard dialog = new Wizard( panel, "sdad", tabs );
		//		dialog.show();

		// ScenicView.show( primaryStage.getScene() );
	}

	public static void main( String[] args )
	{
		Application.launch( args );
	}
}
