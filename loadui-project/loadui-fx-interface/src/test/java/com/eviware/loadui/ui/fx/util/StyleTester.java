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
<<<<<<< HEAD
import javafx.scene.Group;
=======
import javafx.scene.GroupBuilder;
>>>>>>> 09b36c7523c5e2d3a89070dc9112696d4632ad8d
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
import javafx.stage.Stage;

<<<<<<< HEAD
import com.eviware.loadui.ui.fx.control.OptionsSlider;
=======
import com.eviware.loadui.ui.fx.control.Knob;
>>>>>>> 09b36c7523c5e2d3a89070dc9112696d4632ad8d
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

public class StyleTester extends Application
{
	private Node createTestNode()
	{
		//		Carousel<Rectangle> carousel = new Carousel<>( "Hello world" );
		//		carousel.getItems().setAll( new Rectangle( 50, 50, Color.RED ), new Rectangle( 50, 50, Color.BLUE ),
		//				new Rectangle( 50, 50, Color.YELLOW ) );
		//		return carousel;

		Group g = new Group();
		OptionsSlider slider = new OptionsSlider( ImmutableList.of( "Sec", "Min", "Hour" ) );
		g.getChildren().setAll( slider );
		return g;

		Knob knob = new Knob( "Knob", 0, 100, 20 );

		return GroupBuilder.create().children( knob ).build();
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
