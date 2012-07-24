package com.eviware.loadui.ui.fx.views.workspace;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuButtonBuilder;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.TooltipBuilder;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.Properties;

public class ProjectRefView extends VBox
{
	private final ProjectRef projectRef;
	private final ReadOnlyStringProperty labelProperty;

	public ProjectRefView( final ProjectRef projectRef )
	{
		this.projectRef = projectRef;
		getStyleClass().setAll( "project-ref-node" );
		setStyle( "-fx-background-color: black, darkgrey; -fx-background-insets: 0, 2; -fx-background-radius: 5; -fx-padding: 5; -fx-spacing: 5;" );

		labelProperty = Properties.forLabel( projectRef );

		MenuButton menuButton = MenuButtonBuilder.create()
				.tooltip( TooltipBuilder.create().text( projectRef.getProjectFile().getAbsolutePath() ).build() )
				.items( MenuItemBuilder.create().text( "Open" ).onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						openProject();
					}
				} ).build(), MenuItemBuilder.create().text( "Clone" ).onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						//TODO: Open Clone project dialog.
					}
				} ).build(), MenuItemBuilder.create().text( "Delete" ).onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						//TODO: Open Delete dialog?
						projectRef.delete( false );
					}
				} ).build() ).build();
		menuButton.textProperty().bind( labelProperty );

		setPrefWidth( 130 );
		setMaxHeight( 90 );
		setMinHeight( 90 );

		Region region = RegionBuilder.create().style( "-fx-background-color: pink;" )
				.onMouseClicked( new EventHandler<MouseEvent>()
				{
					@Override
					public void handle( MouseEvent event )
					{
						if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
						{
							openProject();
						}
					}
				} ).build();
		VBox.setVgrow( region, Priority.ALWAYS );

		getChildren().setAll( menuButton, region );
	}

	public ProjectRef getProjectRef()
	{
		return projectRef;
	}

	public void openProject()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, projectRef ) );
	}

	@Override
	public String toString()
	{
		return labelProperty.get();
	}
}
