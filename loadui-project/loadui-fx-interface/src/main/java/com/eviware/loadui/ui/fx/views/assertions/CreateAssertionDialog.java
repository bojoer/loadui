package com.eviware.loadui.ui.fx.views.assertions;

import javafx.scene.Node;

import com.eviware.loadui.ui.fx.control.ConfirmationDialog;

public class CreateAssertionDialog extends ConfirmationDialog
{
	public CreateAssertionDialog( Node owner )
	{
		super( owner, "Create assertion", "Create" );
	}

}
