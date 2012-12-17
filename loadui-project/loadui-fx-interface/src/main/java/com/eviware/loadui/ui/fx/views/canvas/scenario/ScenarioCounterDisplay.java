package com.eviware.loadui.ui.fx.views.canvas.scenario;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

import javax.annotation.Nonnull;

import com.eviware.loadui.ui.fx.views.canvas.CounterDisplay;
import com.eviware.loadui.util.StringUtils;

public class ScenarioCounterDisplay extends CounterDisplay
{
	public ScenarioCounterDisplay( @Nonnull String name, @Nonnull Formatting format )
	{
		this.formatting = format;
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
		if( formatting == Formatting.TIME )
			numberDisplay.setText( StringUtils.toHhMmSs( value ) );
		else
			numberDisplay.setText( String.valueOf( value ) );
	}
}
