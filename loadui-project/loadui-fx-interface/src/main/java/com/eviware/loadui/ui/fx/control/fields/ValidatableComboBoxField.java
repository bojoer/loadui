package com.eviware.loadui.ui.fx.control.fields;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.control.ComboBox;

public class ValidatableComboBoxField<T> extends ComboBox<T> implements Field<T>
{
	private final BooleanProperty isValidProperty = new ReadOnlyBooleanWrapper( true );

	@Override
	public T getFieldValue()
	{
		return getSelectionModel().getSelectedItem();
	}

	@Override
	public ReadOnlyBooleanProperty isValidProperty()
	{
		return isValidProperty;
	}

	@Override
	public boolean isValid()
	{
		return isValidProperty().get();
	}
}
