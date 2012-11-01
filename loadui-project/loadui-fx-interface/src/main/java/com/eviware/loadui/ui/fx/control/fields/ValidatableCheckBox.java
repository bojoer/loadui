package com.eviware.loadui.ui.fx.control.fields;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.CheckBox;

import com.eviware.loadui.ui.fx.util.Properties;

public class ValidatableCheckBox extends CheckBox implements Field<Boolean>, Validatable
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
	public ReadOnlyBooleanProperty isValidProperty()
	{
		return Properties.TRUE;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

}
