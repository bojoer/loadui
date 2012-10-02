package com.eviware.loadui.ui.fx.views.canvas;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

import javax.annotation.Nonnull;

public class ScenarioCounterDisplay extends CounterDisplay
{
	public ScenarioCounterDisplay( @Nonnull String name, @Nonnull Formatting format )
	{
		numberDisplay = numberDisplay();

		Label label = label( name );

		getChildren().setAll( numberDisplay, label );
		setAlignment( Pos.CENTER );
		setMinWidth( 45 );
	}

	public ScenarioCounterDisplay( String name )
	{
		this( name, Formatting.NONE );
	}

	@Override
	public void setValue( long value )
	{
		numberDisplay.setText( Long.toString( value ) );
	}
}
