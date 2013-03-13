package com.eviware.loadui.ui.fx.views.canvas;

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
		return LabelBuilder.create().text( name ).style( "-fx-font-size: 9px;" ).build();
	}

	public static Label numberDisplay()
	{
		return LabelBuilder
				.create()
				.maxWidth( Double.MAX_VALUE )
				.style(
						"-fx-background-color: linear-gradient(to bottom, #545454 0%, #000000 50%, #000000 100%); -fx-text-fill: #f2f2f2; -fx-background-radius: 5; -fx-font-size: 9px; -fx-label-padding: 1 7 1 7; -fx-border-width: 2; -fx-border-color: #333333; -fx-border-radius: 4; " )
				.build();
	}
}
