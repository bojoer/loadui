package com.eviware.loadui.ui.fx.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;

import com.eviware.loadui.ui.fx.views.analysis.ChartScrollBar;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

//import com.javafx.experiments.scenicview.ScenicView;

public class StyleTester extends Application
{
	Node testNode = null;

	private Node createTestNode()
	{
		//final int //span = 30;

		int maxval = 130000;

		final DoubleProperty position = new SimpleDoubleProperty();

		final VBox vbox = new VBox();
		final VBox svbox = new VBox();
		final ChartScrollBar csb = new ChartScrollBar();
		final Slider l = new Slider( 0d, maxval, 0d );
		Slider actual1 = new Slider( 0d, maxval, 0d );
		Slider actual2 = new Slider( 0d, maxval, 0d );
		final Slider span = new Slider( 0d, maxval, 0d );
		Slider val = new Slider( 0d, maxval, 0d );
		actual1.disableProperty().set( true );
		actual2.disableProperty().set( true );
		val.disableProperty().set( true );
		span.setValue( 30 );

		//l.setMax( 100 - span );

		Label sl = new Label();
		sl.textProperty().bind( l.valueProperty().asString() );
		Label mp = new Label();
		mp.textProperty().bind( csb.maxProperty().asString().concat( " maxp" ) );
		Label v = new Label( "not set" );
		v.textProperty().bind( csb.valueProperty().asString().concat( " valuep" ) );
		Label a = new Label( "not set" );
		a.textProperty().bind(
				Bindings.max( 0d, csb.valueProperty().subtract( span.valueProperty() ) ).asString().concat( " v-s" ) );
		Label vs = new Label( "not set" );
		vs.textProperty().bind( csb.visibleAmountProperty().asString().concat( " visible" ) );
		Label a1 = new Label( "not set" );
		a1.textProperty().bind( actual1.valueProperty().asString() );
		Label a2 = new Label( "not set" );
		a2.textProperty().bind( actual2.valueProperty().asString() );
		Label s = new Label( "not set" );
		s.textProperty().bind( span.valueProperty().asString().concat( " visible" ) );

		csb.blockIncrementProperty().bind( span.valueProperty().divide( 2 ) );
		csb.unitIncrementProperty().bind( span.valueProperty().divide( 40 ) );

		//		sb.valueProperty().addListener( new InvalidationListener()
		//		{
		//
		//			@Override
		//			public void invalidated( Observable arg0 )
		//			{
		//				double newPosition = sb.valueProperty().get();
		//				double dataLenght = sb.maxProperty().get();
		//				double span2 = span.valueProperty().get();
		//				double margin = 2000d;
		//
		//				double factor = Math.max( 0, dataLenght - span2 + margin ) / Math.max( 1, dataLenght );
		//
		//				position.set( ( long )( newPosition * factor ) );
		//
		//			}
		//		} );

		//		DoubleBinding transform = sb.valueProperty().multiply(
		//				Bindings.max( 0,
		//						sb.maxProperty().subtract( span.valueProperty() ).divide( Bindings.max( 1, sb.maxProperty() ) ) ) );

		position.bind( csb.leftSidePositionProperty() );

		actual1.valueProperty().bind( position );
		actual2.valueProperty().bind( position.add( span.valueProperty().add( 2000d ) ) );

		actual1.maxProperty().bind( l.valueProperty() );
		actual2.maxProperty().bind( l.valueProperty() );

		csb.visibleAmountProperty().bind( span.valueProperty() );

		//l.valueProperty().a

		csb.maxProperty().bind( l.valueProperty() );
		//sb.setMin( span );
		val.valueProperty().bind( csb.valueProperty() );
		val.maxProperty().bind( l.valueProperty() );

		svbox.getChildren().addAll( l, sl );

		vbox.getChildren().addAll( span, s, svbox, csb, mp, actual1, a1, actual2, a2, val, v, a,
				LabelBuilder.create().text( csb.getVisibleAmount() + "" ).build(), vs );

		return vbox;
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

		//		ScenicView.show( primaryStage.getScene() );
	}

	public static void main( String[] args )
	{
		Application.launch( args );
	}
}
