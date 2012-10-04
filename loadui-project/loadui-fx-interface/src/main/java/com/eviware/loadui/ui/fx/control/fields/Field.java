package com.eviware.loadui.ui.fx.control.fields;

public interface Field<T>
{
	T getValue();

	public interface Validatable<T> extends Field<T>
	{
		boolean validate();
	}
}
