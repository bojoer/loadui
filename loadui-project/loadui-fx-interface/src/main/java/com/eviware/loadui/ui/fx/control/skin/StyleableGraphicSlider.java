package com.eviware.loadui.ui.fx.control.skin;

import javafx.application.Platform;
import javafx.scene.control.Slider;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;

import com.sun.javafx.scene.control.skin.SliderSkin;

public class StyleableGraphicSlider extends SliderSkin
{
	public StyleableGraphicSlider(final Slider slider){
		super(slider);
		Platform.runLater( new Runnable(){
			public void run(){
				StackPane thumb = (StackPane) slider.lookup( ".thumb" );
				thumb.getChildren().add(RegionBuilder.create().styleClass( "graphic" ).stylesheets( "StyleableGraphicsSlider.css" ).minHeight( 6 ).minWidth( 12 ).build());
			}
		});
	}
}
