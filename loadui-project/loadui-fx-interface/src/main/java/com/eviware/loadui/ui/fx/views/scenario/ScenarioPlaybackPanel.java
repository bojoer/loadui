package com.eviware.loadui.ui.fx.views.scenario;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.views.canvas.ToolbarPlaybackPanel;

final public class ScenarioPlaybackPanel extends ToolbarPlaybackPanel<SceneItem>
{
	public ScenarioPlaybackPanel( SceneItem canvas )
	{
		super( canvas );

		getStyleClass().setAll( "scenario-playback-panel" );
		setMaxWidth( 650 );

		getChildren().setAll( separator(), playButton, separator(), time, requests,
				failures, resetButton(), limitsButton() );
	}
}
