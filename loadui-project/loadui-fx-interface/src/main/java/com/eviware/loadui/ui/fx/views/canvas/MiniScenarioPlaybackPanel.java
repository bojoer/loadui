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

		getStyleClass().setAll( "mini-playback-panel" );
		setMaxHeight( 28 );
		setMaxWidth( 255 );
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
