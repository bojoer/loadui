package com.eviware.loadui.ui.fx.control.fields;

import javafx.beans.property.ReadOnlyBooleanProperty;

public interface Validatable
{
	ReadOnlyBooleanProperty isValidProperty();

	boolean isValid();
}