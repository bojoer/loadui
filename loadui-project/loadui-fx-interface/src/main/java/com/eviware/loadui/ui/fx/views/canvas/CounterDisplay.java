package com.eviware.loadui.ui.fx.views.canvas;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.layout.VBox;

public abstract class CounterDisplay extends VBox
{
	protected Label numberDisplay;
	protected Formatting formatting;

	public enum Formatting
	{
		NONE, TIME
	}

	public abstract void setValue( long value );

	public static Label label( String name )
	{
		return LabelBuilder.create().text( name ).minWidth(25).style( "-fx-font-size: 10px;" ).build();
	}

	public static Label numberDisplay()
	{
		
		return LabelBuilder
				.create()
				.minWidth( 45 )
				.prefWidth(50)
				.style("-fx-text-fill: #f2f2f2; -fx-font-size: 10px; ")
				.alignment( Pos.CENTER )
				.build();
	}
}
