package com.eviware.loadui.ui.fx.views.canvas;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.util.Callback;

import com.eviware.loadui.api.counter.CounterHolder;

public class ScenarioPlaybackPanel extends MiniScenarioPlaybackPanel
{
	private final EventHandler<ActionEvent> resetCounters = new EventHandler<ActionEvent>()
	{
		@Override
		public void handle( ActionEvent e )
		{
			canvas.triggerAction( CounterHolder.COUNTER_RESET_ACTION );
		}
	};

	public final EventHandler<ActionEvent> openLimitsDialog = new EventHandler<ActionEvent>()
	{
		@Override
		public void handle( ActionEvent e )
		{
			LimitsDialog.instanceOf( ScenarioPlaybackPanel.this ).show();
		}
	};

	protected final CounterDisplay time;

	protected final CounterDisplay requests;

	protected final CounterDisplay failures;

	public ScenarioPlaybackPanel()
	{
		setStyle( "-fx-spacing: 8; -fx-background-color: #8b8c8f; -fx-background-radius: 7;" );
		setMaxHeight( 28 );
		setMaxWidth( 550 );
		setAlignment( Pos.CENTER );

		ToggleButton playButton = new ToggleButton();
		playButton.selectedProperty().addListener( playCanvas );
		ProgressIndicator playSpinner = new ProgressIndicator();
		playSpinner.visibleProperty().bind( playButton.selectedProperty() );
		playButton.textProperty().bind(
				Bindings.when( playButton.selectedProperty() ).then( "\u25FC" ).otherwise( "\u25B6" ) );
		StackPane playStack = StackPaneBuilder.create().children( playSpinner, playButton ).build();

		ComboBox<Image> distibutionMode = new ComboBox<>( FXCollections.observableArrayList( image( "mode-local.png" ),
				image( "mode-distributed.png" ) ) );
		final Callback<ListView<Image>, ListCell<Image>> cellFactory = new Callback<ListView<Image>, ListCell<Image>>()
		{
			@Override
			public ListCell<Image> call( ListView<Image> arg0 )
			{
				return new ListCell<Image>()
				{
					private final ImageView imageView = new ImageView();

					@Override
					protected void updateItem( Image item, boolean empty )
					{
						super.updateItem( item, empty );

						if( item == null || empty )
						{
							setGraphic( null );
						}
						else
						{
							imageView.setImage( item );
							setGraphic( imageView );
						}
					}
				};
			}
		};
		distibutionMode.setCellFactory( cellFactory );
		distibutionMode.setButtonCell( cellFactory.call( null ) );

		distibutionMode.getSelectionModel().selectFirst();

		time = new CounterDisplay( "Time" );
		requests = new CounterDisplay( "Requests" );
		failures = new CounterDisplay( "Failures" );

		Button resetButton = ButtonBuilder.create().text( "Reset" ).style( "-fx-font-size: 10px;" )
				.onAction( resetCounters ).build();
		Button limitsButton = ButtonBuilder.create().text( "Limits\u2026" ).style( "-fx-font-size: 10px;" )
				.onAction( openLimitsDialog ).build();
		getChildren().setAll( playStack, distibutionMode, separator(), time, separator(), requests, separator(),
				failures, separator(), resetButton, limitsButton );
	}

	private Image image( String name )
	{
		return new Image( getClass().getResourceAsStream( name ) );
	}
}
