package com.eviware.loadui.ui.fx.views.scenario;

import javafx.geometry.Pos;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.views.canvas.ToolbarPlaybackPanel;

final public class ScenarioPlaybackPanel extends ToolbarPlaybackPanel<SceneItem>
{
	public ScenarioPlaybackPanel( SceneItem canvas )
	{
		super( canvas );

		setStyle( "-fx-spacing: 8; -fx-background-color: #8b8c8f; -fx-background-radius: 7;" );
		setMaxHeight( 28 );
		setMaxWidth( 550 );
		setAlignment( Pos.CENTER );

		getChildren().setAll( playButton, separator(), linkScenarioButton( canvas ), separator(), time, separator(),
				requests, separator(), failures, separator(), resetButton(), limitsButton() );
	}
}
