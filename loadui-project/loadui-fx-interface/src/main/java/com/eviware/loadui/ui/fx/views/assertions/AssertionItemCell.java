package com.eviware.loadui.ui.fx.views.assertions;

import javafx.scene.control.ListCell;

import com.eviware.loadui.api.assertion.AssertionItem;

public class AssertionItemCell extends ListCell<AssertionItem>
{
	@Override
	public void updateItem( AssertionItem item, boolean empty )
	{
		super.updateItem( item, empty );
		if( !empty )
		{
			setText( item.getLabel() );
		}
	}
}
