package com.eviware.loadui.ui.fx.control.fields;

import javafx.scene.Node;

public class ValidatableFieldSupport
{
	public static final String INVALID_CLASS = "invalid";

	public static void setInvalid( Node parent )
	{
		parent.getStyleClass().add( INVALID_CLASS );
	}

	public static void setValid( Node parent )
	{
		parent.getStyleClass().remove( INVALID_CLASS );
	}
}
