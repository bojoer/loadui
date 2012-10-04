package com.eviware.loadui.ui.fx.control.fields;

import javafx.scene.control.CheckBox;

public class ValidatableCheckBox extends CheckBox implements Field.Validatable<Boolean>
{
	public ValidatableCheckBox( String label )
	{
		super( label );
	}

	@Override
	public Boolean getValue()
	{
		return isSelected();
	}

	@Override
	public boolean validate()
	{
		return true;
	}

}
