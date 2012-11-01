package com.eviware.loadui.ui.fx.views.assertions;

import javafx.application.Platform;
import javafx.scene.control.ListCell;

import com.eviware.loadui.api.assertion.AssertionItem;

public class AssertionItemCell extends ListCell<AssertionItem>
{
	@Override
	public void updateItem( final AssertionItem assertion, boolean empty )
	{
		super.updateItem( assertion, empty );
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				if( assertion != null )
				{
					setGraphic( new AssertionView( assertion ) );
				}
			}
		} );
	}
}
