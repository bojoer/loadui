package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.scene.control.Label;

public abstract class SegmentView extends Label
{
	public void setColor( String color )
	{
		setStyle( "-fx-background-color: " + color + ";" );
	}
}
