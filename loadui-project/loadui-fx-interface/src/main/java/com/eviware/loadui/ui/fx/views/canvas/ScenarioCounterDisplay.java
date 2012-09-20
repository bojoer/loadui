package com.eviware.loadui.ui.fx.views.canvas;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ScenarioCounterDisplay extends VBox
{
	private final Label numberDisplay;

	public ScenarioCounterDisplay( String name )
	{
		numberDisplay = CounterDisplay.numberDisplay();

		Label label = CounterDisplay.label( name );

		getChildren().setAll( numberDisplay, label );
		setAlignment( Pos.CENTER );
		setMinWidth( 45 );
	}

	public void setValue( long value )
	{
		numberDisplay.setText( Long.toString( value ) );
	}
}
