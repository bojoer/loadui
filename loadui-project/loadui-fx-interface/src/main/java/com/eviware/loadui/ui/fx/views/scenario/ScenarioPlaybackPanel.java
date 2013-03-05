package com.eviware.loadui.ui.fx.views.scenario;

import javafx.geometry.Pos;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.views.canvas.ToolbarPlaybackPanel;

final public class ScenarioPlaybackPanel extends ToolbarPlaybackPanel<SceneItem>
{
	public ScenarioPlaybackPanel( SceneItem canvas )
	{
		super( canvas );

		getStyleClass().setAll( "scenario-playback-panel" );
		setSpacing( 6 );
		setMaxHeight( 27 );
		setMaxWidth( 650 );
		setAlignment( Pos.CENTER );

		getChildren().setAll( separator(), playButton, separator(), linkScenarioButton( canvas ), time, requests,
				failures, resetButton(), limitsButton() );
	}
}
