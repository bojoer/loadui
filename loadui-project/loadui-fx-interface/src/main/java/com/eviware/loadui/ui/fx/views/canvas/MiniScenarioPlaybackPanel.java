package com.eviware.loadui.ui.fx.views.canvas;

import com.eviware.loadui.ui.fx.views.canvas.CounterDisplay.Formatting;

import javafx.geometry.Pos;

public class MiniScenarioPlaybackPanel extends PlaybackPanel
{
	public MiniScenarioPlaybackPanel()
	{
		setStyle( "-fx-spacing: 8; -fx-background-color: #8b8c8f; -fx-background-radius: 7;" );
		setMaxHeight( 28 );
		setMaxWidth( 245 );
		setAlignment( Pos.CENTER );

		getChildren().setAll( playStack(), separator(), time, separator(), requests, separator(), failures );
	}

	@Override
	protected CounterDisplay timeCounter()
	{
		return new ScenarioCounterDisplay( TIME, Formatting.TIME );
	}

	@Override
	protected CounterDisplay timeRequests()
	{
		return new ScenarioCounterDisplay( TIME );
	}

	@Override
	protected CounterDisplay timeFailures()
	{
		return new ScenarioCounterDisplay( TIME );
	}
}
