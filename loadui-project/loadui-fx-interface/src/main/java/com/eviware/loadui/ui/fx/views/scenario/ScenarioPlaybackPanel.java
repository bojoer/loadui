package com.eviware.loadui.ui.fx.views.scenario;

import com.eviware.loadui.ui.fx.views.canvas.CounterDisplay;
import com.eviware.loadui.ui.fx.views.canvas.CounterDisplay.Formatting;
import com.eviware.loadui.ui.fx.views.canvas.PlaybackPanel;
import com.eviware.loadui.ui.fx.views.canvas.ToolbarCounterDisplay;

import javafx.geometry.Pos;

public class ScenarioPlaybackPanel extends PlaybackPanel
{
	public ScenarioPlaybackPanel()
	{
		setStyle( "-fx-spacing: 8; -fx-background-color: #8b8c8f; -fx-background-radius: 7;" );
		setMaxHeight( 28 );
		setMaxWidth( 550 );
		setAlignment( Pos.CENTER );

		getChildren().setAll( playStack(), separator(), time, separator(), requests, separator(), failures, separator(),
				resetButton(), limitsButton() );
	}

	@Override
	protected CounterDisplay timeCounter()
	{
		return new ToolbarCounterDisplay( TIME, Formatting.TIME );
	}

	@Override
	protected CounterDisplay timeRequests()
	{
		return new ToolbarCounterDisplay( REQUESTS );
	}

	@Override
	protected CounterDisplay timeFailures()
	{
		return new ToolbarCounterDisplay( FAILURES );
	}
}
