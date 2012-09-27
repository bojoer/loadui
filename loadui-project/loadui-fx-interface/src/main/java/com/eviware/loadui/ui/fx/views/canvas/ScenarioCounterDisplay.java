package com.eviware.loadui.ui.fx.views.canvas;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class ScenarioCounterDisplay extends CounterDisplay
{
	private final Label numberDisplay;

	public ScenarioCounterDisplay( String name )
	{
		numberDisplay = ToolbarCounterDisplay.numberDisplay();

		Label label = ToolbarCounterDisplay.label( name );

		getChildren().setAll( numberDisplay, label );
		setAlignment( Pos.CENTER );
		setMinWidth( 45 );
	}

	@Override
	public void setValue( long value )
	{
		numberDisplay.setText( Long.toString( value ) );
	}
}
