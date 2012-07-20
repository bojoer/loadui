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
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.Dialog;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.views.workspace.CreateNewProjectDialog;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class StyleTester extends Application
{
	private Node createTestNode()
	{
		ToolBox<Label> toolBox = new ToolBox<>( "Toolbox" );
		final Label rectangle1 = LabelBuilder.create().styleClass( "label", "icon" ).text( "Rectangle" )
				.graphic( RectangleBuilder.create().width( 45 ).height( 50 ).fill( Color.RED ).build() ).build();
		final Label rectangle2 = LabelBuilder.create().styleClass( "label", "icon" ).text( "Another Rectangle" )
				.graphic( RectangleBuilder.create().width( 45 ).height( 50 ).fill( Color.BLUE ).build() ).build();
		final Label rectangle3 = LabelBuilder.create().styleClass( "label", "icon" ).text( "Rectangle" )
				.graphic( RectangleBuilder.create().width( 60 ).height( 60 ).fill( Color.GREEN ).build() ).build();
		final Label rectangle4 = LabelBuilder.create().styleClass( "label", "icon" ).text( "Rectangle" )
				.graphic( RectangleBuilder.create().width( 60 ).height( 60 ).fill( Color.GREEN ).build() ).build();

		ToolBox.setCategory( rectangle1, "Category 1" );
		ToolBox.setCategory( rectangle2, "Category 2" );
		ToolBox.setCategory( rectangle3, "Category 3" );
		ToolBox.setCategory( rectangle4, "Category 2" );

		toolBox.getItems().setAll( rectangle1, rectangle2, rectangle3, rectangle4 );

		return toolBox;
	}

	@Override
	public void start( final Stage primaryStage ) throws Exception
	{
		final StackPane panel = new StackPane();
		panel.getChildren().setAll( createTestNode() );

		final TextArea styleArea = TextAreaBuilder.create().build();

		final File styleSheet = File.createTempFile( "style", ".css" );

		System.out.println( styleSheet.toURI().toURL().toExternalForm() );

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
										.setAll( "file:/src/main/resources/com/eviware/loadui/ui/fx/loadui-style.css",
												styleSheet.toURI().toURL().toExternalForm() );
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

		System.out.println( new File( "." ).getAbsolutePath() );

		VBox.setVgrow( styleArea, Priority.ALWAYS );

		primaryStage.setScene( SceneBuilder
				.create()
				.width( 640 )
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

		final Dialog dialog = new CreateNewProjectDialog( primaryStage.getScene() );
		dialog.show();
	}

	public static void main( String[] args )
	{
		Application.launch( args );
	}
}
