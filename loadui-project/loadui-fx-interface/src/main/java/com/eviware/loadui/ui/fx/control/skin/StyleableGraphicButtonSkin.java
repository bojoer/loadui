package com.eviware.loadui.ui.fx.control.skin;

import javafx.scene.control.Button;
import javafx.scene.layout.RegionBuilder;

import com.sun.javafx.scene.control.skin.ButtonSkin;

public class StyleableGraphicButtonSkin extends ButtonSkin
{
	public StyleableGraphicButtonSkin( Button button )
	{
		super( button );

		button.setGraphic( RegionBuilder.create().styleClass( "graphic" ).build() );
	}
}
