package com.eviware.loadui.ui.fx.views.project;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import com.eviware.loadui.ui.fx.views.canvas.ToolbarPlaybackPanel;

final public class ProjectPlaybackPanel extends ToolbarPlaybackPanel
{
	public ProjectPlaybackPanel()
	{
		setStyle( "-fx-spacing: 8; -fx-background-color: #8b8c8f; -fx-background-radius: 7;" );
		setMaxHeight( 28 );
		setMaxWidth( 550 );
		setAlignment( Pos.CENTER );

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

		getChildren().setAll( playButton, separator(), distibutionMode, separator(), time, separator(), requests,
				separator(), failures, separator(), resetButton(), limitsButton() );
	}
}
