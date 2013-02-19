package com.eviware.loadui.ui.fx.control.skin;

import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.RegionBuilder;

import com.sun.javafx.scene.control.skin.ToggleButtonSkin;

public class StyleableGraphicToggleButtonSkin extends ToggleButtonSkin
{

	public StyleableGraphicToggleButtonSkin( ToggleButton button )
	{
		super( button );
		
		button.setGraphic( HBoxBuilder.create().children(RegionBuilder.create().styleClass( "graphic" ).build(), RegionBuilder.create().styleClass( "secondary-graphic" ).build()).build());
	}
}
