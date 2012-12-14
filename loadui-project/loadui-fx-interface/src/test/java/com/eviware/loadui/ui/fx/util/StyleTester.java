package com.eviware.loadui.ui.fx.util;

import static javafx.beans.binding.Bindings.when;

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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;

import com.eviware.loadui.ui.fx.views.analysis.ZoomMenuButton;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

//import com.javafx.experiments.scenicview.ScenicView;

public class StyleTester extends Application
{
	Node testNode = null;

	private Node createTestNode()
	{
		final HBox box = new HBox();
		ZoomMenuButton zmb1 = new ZoomMenuButton();
		ZoomMenuButton zmb2 = new ZoomMenuButton();

		//		zmb1.getToggleGroup().selectedToggleProperty().addListener( new ChangeListener<Toggle>()
		//		{
		//
		//			@Override
		//			public void changed( ObservableValue<? extends Toggle> arg0, Toggle arg1, Toggle newToggle )
		//			{
		//				System.out.println( "Trying to set ZoomLevel to: " + newToggle );
		//
		//			}
		//
		//		} );

		ToggleButton b = ToggleButtonBuilder.create().text( "toggle" ).build();

		box.styleProperty().bind(
				when( b.selectedProperty() ).then( "-fx-background-color: pink;" ).otherwise(
						"-fx-background-color: yellow;" ) );

		box.getChildren().addAll( zmb1, zmb2, b );

		return box;
	}

	@Override
	public void start( final Stage primaryStage ) throws Exception
	{
		final StackPane panel = new StackPane();
		panel.getChildren().setAll( createTestNode() );

		final TextArea styleArea = TextAreaBuilder.create().build();

		final File styleSheet = File.createTempFile( "style", ".css" );

		final String externalForm = new File( "src/main/resources/com/eviware/loadui/ui/fx/loadui-style.css" ).toURI()
				.toURL().toExternalForm();
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
								styleArea.getScene().getStylesheets()
										.setAll( externalForm, styleSheet.toURI().toURL().toExternalForm() );
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

		primaryStage.getScene().getStylesheets().setAll( externalForm );

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
