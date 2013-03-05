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
		setSpacing( 6 );
		setMaxHeight( 27 );
		setMaxWidth( 610 );
		setAlignment( Pos.CENTER );

		getChildren().setAll( separator(), playButton, separator(), time, requests, failures, resetButton(),
				limitsButton() );
	}
}
