package com.eviware.loadui.ui.fx.views.canvas;

import javafx.geometry.Pos;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.views.canvas.CounterDisplay.Formatting;
import com.eviware.loadui.ui.fx.views.canvas.scenario.ScenarioCounterDisplay;

public class MiniScenarioPlaybackPanel extends PlaybackPanel<CounterDisplay, SceneItem>
{
	public MiniScenarioPlaybackPanel( SceneItem canvas )
	{
		super( canvas );

		setStyle( "-fx-spacing: 8; -fx-background-color: #8b8c8f; -fx-background-radius: 7;" );
		setMaxHeight( 28 );
		setMaxWidth( 245 );
		setAlignment( Pos.CENTER );

		getChildren().setAll( playButton, separator(), linkScenarioButton( canvas ), separator(), time, separator(),
				requests, separator(), failures );
	}

	@Override
	protected CounterDisplay timeCounter()
	{
		return new ScenarioCounterDisplay( TIME_LABEL, Formatting.TIME );
	}

	@Override
	protected CounterDisplay timeRequests()
	{
		return new ScenarioCounterDisplay( TIME_LABEL );
	}

	@Override
	protected CounterDisplay timeFailures()
	{
		return new ScenarioCounterDisplay( TIME_LABEL );
	}
}
