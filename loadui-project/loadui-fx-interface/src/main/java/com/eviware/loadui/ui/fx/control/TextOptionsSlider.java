package com.eviware.loadui.ui.fx.control;

import java.util.List;

import javafx.scene.control.RadioButton;

import com.eviware.loadui.ui.fx.util.UIUtils;

public class TextOptionsSlider extends OptionsSlider<String>
{
	public TextOptionsSlider( List<String> options )
	{
		super( options );
	}

	@Override
	public void labelRadioButton( RadioButton radio, int index )
	{
		radio.setText( options.get( index ) );
		radio.setId( UIUtils.toCssId( options.get( index ) ) );
	}
}
