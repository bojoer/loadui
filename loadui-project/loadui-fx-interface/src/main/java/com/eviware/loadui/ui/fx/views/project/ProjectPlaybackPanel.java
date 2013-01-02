package com.eviware.loadui.ui.fx.views.project;

import javafx.geometry.Pos;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.ui.fx.views.canvas.ToolbarPlaybackPanel;

final public class ProjectPlaybackPanel extends ToolbarPlaybackPanel<ProjectItem>
{
	public ProjectPlaybackPanel( ProjectItem canvas )
	{
		super( canvas );

		getStyleClass().add( "project-playback-panel" );
		setStyle( "-fx-spacing: 8; -fx-background-color: #8b8c8f; -fx-background-radius: 7;" );
		setMaxHeight( 28 );
		setMaxWidth( 550 );
		setAlignment( Pos.CENTER );

		getChildren().setAll( playButton, separator(), time, separator(), requests, separator(), failures, separator(),
				resetButton(), limitsButton() );
	}
}
