package com.eviware.loadui.ui.fx.control.fields;

import javafx.scene.Node;

public class ValidatableFieldSupport
{
	public static final String INVALID_CLASS = "invalid";

	public static <T extends Node & Field.Validatable<?>> void setInvalid( T parent )
	{
		parent.getStyleClass().add( INVALID_CLASS );
	}

	public static <T extends Node & Field.Validatable<?>> void setValid( T parent )
	{
		parent.getStyleClass().remove( INVALID_CLASS );
	}
}
