package com.eviware.loadui.ui.fx.control.skin;

import javafx.scene.control.Label;
import javafx.scene.layout.RegionBuilder;

import com.sun.javafx.scene.control.skin.LabelSkin;

public class StyleableGraphicButtonSkin extends LabelSkin
{
	public StyleableGraphicButtonSkin( Label label )
	{
		super( label );

		label.setGraphic( RegionBuilder.create().styleClass( "graphic" ).build() );
	}
}
