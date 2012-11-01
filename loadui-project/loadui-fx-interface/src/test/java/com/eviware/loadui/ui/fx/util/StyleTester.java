package com.eviware.loadui.ui.fx.util;

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
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

import com.eviware.loadui.ui.fx.control.ToolBox;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class StyleTester extends Application
{
	private Node createTestNode()
	{
		ToolBox<Rectangle> toolBox = new ToolBox<>( "Toolbox" );
		Rectangle r1 = RectangleBuilder.create().width( 50 ).height( 50 ).fill( Color.RED ).build();
		ToolBox.setCategory( r1, "Cat 1" );
		Rectangle r2 = RectangleBuilder.create().width( 50 ).height( 50 ).fill( Color.BLUE ).build();
		ToolBox.setCategory( r2, "Cat 2" );
		Rectangle r3 = RectangleBuilder.create().width( 50 ).height( 50 ).fill( Color.BLUE ).build();
		ToolBox.setCategory( r3, "Cat 2" );
		toolBox.getItems().addAll( r1, r2, r3 );
		return toolBox;
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
								styleArea
										.getScene()
										.getStylesheets()
										.setAll( "src/main/resources/com/eviware/loadui/ui/fx/loadui-style.css",
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
																		panel.getChildren().setAll( createTestNode() );
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
