package com.eviware.loadui.components.soapui.layout;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class StyleTester extends Application
{
	private Node createTestNode( Stage stage )
	{
		//		Node n =  SoapUiProjectSelector.buildNode();
		//
		//		return GroupBuilder.create().children( n ).build();
		ObservableList<String> ol = FXCollections.<String> observableArrayList( "a", "b" );
		final ComboBox<String> c = ComboBoxBuilder.<String> create().items( ol ).build();
		Button b = new Button( "Change options" );
		final Label l = new Label( "" );
		c.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<String>()
		{
			@Override
			public void changed( ObservableValue<? extends String> arg0, String arg1, String val )
			{
				l.setText( l.getText() + ";" + val );
			}
		} );
		b.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent e )
			{
				c.setItems( FXCollections.<String> observableArrayList( "x", "y" ) );
			}
		} );
		return HBoxBuilder.create().children( c, b, l ).build();
	}

	@Override
	public void start( final Stage primaryStage ) throws Exception
	{
		final StackPane panel = new StackPane();
		panel.getChildren().setAll( createTestNode( primaryStage ) );

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
								styleArea
										.getScene()
										.getStylesheets()
										.setAll( "/com/eviware/loadui/ui/fx/loadui-style.css",
												styleSheet.toURI().toURL().toExternalForm() );
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
														ButtonBuilder.create().text( "Rebuild" )
																.onAction( new EventHandler<ActionEvent>()
																{
																	@Override
																	public void handle( ActionEvent arg0 )
																	{
																		panel.getChildren().setAll( createTestNode( primaryStage ) );
																	}
																} ).build() ).build() ).build() ).build() );

		primaryStage
				.getScene()
				.getStylesheets()
				.setAll(
						new File( "src/main/resources/com/eviware/loadui/ui/fx/loadui-style.css" ).toURI().toURL()
								.toExternalForm() );

		primaryStage.show();

		//		final Dialog dialog = new ConfirmationDialog( panel, "sdad", "2d" );
		//		dialog.show();
	}

	public static void main( String[] args )
	{
		Application.launch( args );
	}
}
