package com.eviware.loadui.ui.fx.util;

import static com.eviware.loadui.ui.fx.util.ObservableLists.filter;
import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
<<<<<<< HEAD
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
=======
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
>>>>>>> 4e3764ee85f42054fe038d0d86483c65d52c321a
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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

<<<<<<< HEAD
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.ui.fx.views.result.ResultView;
=======
import com.eviware.loadui.ui.fx.views.analysis.ChartScrollBar;
>>>>>>> 4e3764ee85f42054fe038d0d86483c65d52c321a
import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.io.Files;

//import com.javafx.experiments.scenicview.ScenicView;

public class StyleTester extends Application
{
	Node testNode = null;

	private Node createTestNode()
	{
<<<<<<< HEAD

		EventFirer firer = mock( EventFirer.class );

		Execution ex1 = mock( Execution.class );
		when( ex1.getLabel() ).thenReturn( "Ex 1" );
		when( ex1.isArchived() ).thenReturn( false );

		Execution ex2 = mock( Execution.class );
		when( ex2.getLabel() ).thenReturn( "Ex 2" );
		when( ex2.isArchived() ).thenReturn( false );

		Execution ex3 = mock( Execution.class );
		when( ex3.getLabel() ).thenReturn( "Ex 3" );
		when( ex3.isArchived() ).thenReturn( true );

		List<Execution> executions = Arrays.asList( ex1, ex2, ex3 );

		Execution curr = mock( Execution.class );
		when( curr.getLabel() ).thenReturn( "Current" );
		when( curr.isArchived() ).thenReturn( false );
		Property<Execution> currentExecution = new SimpleObjectProperty<>( curr, "currentExecution" );

		ObservableList<Execution> recentExecutions = fx( filter(
				ofCollection( firer, ExecutionManager.RECENT_EXECUTIONS, Execution.class, executions ),
				new Predicate<Execution>()
				{
					@Override
					public boolean apply( Execution input )
					{
						return !input.isArchived();
					}
				} ) );
		ObservableList<Execution> archivedExecutions = fx( filter(
				ofCollection( firer, ExecutionManager.RECENT_EXECUTIONS, Execution.class, executions ),
				new Predicate<Execution>()
				{
					@Override
					public boolean apply( Execution input )
					{
						return input.isArchived();
					}
				} ) );
		ResultView view = new ResultView( currentExecution, recentExecutions, archivedExecutions );
		return view;
=======
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
>>>>>>> 4e3764ee85f42054fe038d0d86483c65d52c321a
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
