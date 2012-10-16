package com.eviware.loadui.ui.fx.control;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;

import com.google.common.collect.ImmutableList;

abstract public class OptionsSlider<T> extends Control
{
	private static final String DEFAULT_STYLE_CLASS = "options-slider";

	protected final List<T> options;

	private final ObjectProperty<T> selectedProperty = new ObjectPropertyBase<T>()
	{
		@Override
		public Object getBean()
		{
			return OptionsSlider.this;
		}

		@Override
		public String getName()
		{
			return "selected";
		}
	};

	public OptionsSlider( List<T> options )
	{
		this.options = options;
		getStyleClass().setAll( DEFAULT_STYLE_CLASS );
	}

	public List<T> getOptions()
	{
		return ImmutableList.copyOf( options );
	}

	public ObjectProperty<T> selectedProperty()
	{
		return selectedProperty;
	}

	public void setSelected( T value )
	{
		selectedProperty.setValue( value );
	}

	public T getSelected()
	{
		return selectedProperty.getValue();
	}

	public abstract void labelRadioButton( RadioButton radio, int index );
}
