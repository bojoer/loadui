package com.eviware.loadui.ui.fx.control;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPaneBuilder;

import javax.annotation.Nonnull;

public class Wizard extends ConfirmationDialog
{
	@Nonnull
	private final TabPane tabPane = new TabPane();
	@Nonnull
	private final List<SettingsTab> tabs;

	public Wizard( Node owner, String title, @Nonnull List<SettingsTab> tabs )
	{
		super( owner, title, "Next >", true );
		this.tabs = tabs;
		tabPane.getTabs().addAll( tabs );
		addStyleClass( "wizard" );

		HBox steps = HBoxBuilder.create().spacing( 18.0 ).build();
		int i = 1;
		for( Tab t : tabs )
		{
			steps.getChildren().add( new StepIndicator( i++ , t.getText() ) );
		}
		getItems().setAll( steps, tabPane );

		tabPane.setTabMaxHeight( 0.0 );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent e )
			{
				tabPane.getSelectionModel().selectNext();
			}
		} );
	}

	public static class StepIndicator extends Label
	{
		public StepIndicator( int stepNumber, String label )
		{
			setText( label );
			getStyleClass().add( "step-label" );
			Label l = LabelBuilder.create().alignment( Pos.CENTER ).prefHeight( 16 ).prefWidth( 16 )
					.text( String.valueOf( stepNumber ) ).build();
			Region r = RegionBuilder.create().prefHeight( 16 ).prefWidth( 16 ).build();
			r.getStyleClass().add( "dot" );
			setGraphic( StackPaneBuilder.create().children( r, l ).build() );
		}
	}

}