package com.eviware.loadui.ui.fx.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.chart.Axis.TickMark;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.AnchorPaneBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import com.eviware.loadui.api.charting.line.ZoomLevel;
import com.eviware.loadui.ui.fx.views.analysis.linechart.LineSegmentView;
import com.eviware.loadui.ui.fx.views.analysis.linechart.SegmentView;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

//import com.javafx.experiments.scenicview.ScenicView;

public class StyleTester extends Application
{
	Node testNode = null;

	private static final PeriodFormatter timeFormatter = new PeriodFormatterBuilder().printZeroNever().appendWeeks()
			.appendSuffix( "w" ).appendSeparator( " " ).appendDays().appendSuffix( "d" ).appendSeparator( " " )
			.appendHours().appendSuffix( "h" ).appendSeparator( " " ).appendMinutes().appendSuffix( "m" ).toFormatter();

	private final long unitLengthInMillis = ZoomLevel.MINUTES.getInterval() * 1000;

	@SuppressWarnings( "rawtypes" )
	private Node createTestNode()
	{
		Label zoomLevel = LabelBuilder.create().text( "Sec" ).build();
		zoomLevel.getStyleClass().add( "overlayed-chart-text" );
		AnchorPane.setRightAnchor( zoomLevel, 0.0 );
		AnchorPane.setBottomAnchor( zoomLevel, 15.0 );
		Label timer = LabelBuilder.create().text( "1h 32m" ).build();
		timer.getStyleClass().add( "overlayed-chart-text" );
		AnchorPane.setLeftAnchor( timer, 33.0 );
		AnchorPane.setBottomAnchor( timer, 15.0 );

		NumberAxis xAxis = new NumberAxis();
		final LineChart chart = new LineChart( xAxis, new NumberAxis() );
		chart.setData( FXCollections.observableArrayList( new XYChart.Series<Number, Number>( FXCollections
				.observableArrayList( new XYChart.Data<Number, Number>( 55000, 5 ), new XYChart.Data<Number, Number>(
						58000, 3 ), new XYChart.Data<Number, Number>( 67000, 9 ) ) ) ) );
		chart.setLegendVisible( false );
		AnchorPane.setTopAnchor( chart, 0.0 );
		AnchorPane.setRightAnchor( chart, 0.0 );
		AnchorPane.setBottomAnchor( chart, 0.0 );
		AnchorPane.setLeftAnchor( chart, 0.0 );
		AnchorPane pane = AnchorPaneBuilder.create().children( chart, zoomLevel, timer ).build();

		xAxis.setTickLabelFormatter( new StringConverter<Number>()
		{
			@Override
			public String toString( Number n )
			{
				long value = n.longValue();
				if( value % unitLengthInMillis != 0 )
				{
					return Long.toString( value / 1000 );
				}
				Period period = new Period( value );
				return timeFormatter.print( period );
			}

			@Override
			public Number fromString( String s )
			{
				// TODO Auto-generated method stub
				return null;
			}
		} );

		xAxis.getChildrenUnmodifiable().addListener( new ListChangeListener<Node>()
		{
			@Override
			public void onChanged( javafx.collections.ListChangeListener.Change<? extends Node> change )
			{
				while( change.next() )
				{
					for( Node newNode : change.getAddedSubList() )
					{
						if( newNode instanceof Text )
						{
							String textContent = ( ( Text )newNode ).getText();
							try
							{
								Long.parseLong( textContent );
							}
							catch( NumberFormatException e )
							{
								System.out.println( textContent );
								//								newNode.setStyle( "-fx-fill: red;" );
								newNode.getStyleClass().add( "overlayed-chart-text" );
							}
						}
					}
				}
			}
		} );

		return pane;
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
