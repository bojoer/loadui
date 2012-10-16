package com.eviware.loadui.ui.fx.control;

import java.util.List;

import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;

public class GraphicOptionsSlider extends OptionsSlider<ImageView>
{
	public GraphicOptionsSlider( List<ImageView> options )
	{
		super( options );
	}

	@Override
	public void labelRadioButton( RadioButton radio, int index )
	{
		radio.setGraphic( options.get( index ) );
	}
}
