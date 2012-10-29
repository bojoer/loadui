package com.eviware.loadui.ui.fx.views.assertions;

import com.eviware.loadui.api.traits.Labeled;

import javafx.scene.control.TreeCell;

public class LabeledTreeCellImpl extends TreeCell<Labeled>
{
	@Override
	public void updateItem( Labeled item, boolean empty )
	{
		super.updateItem( item, empty );

		if( empty )
		{
			setText( null );
		}
		else
		{
			setText( item.getLabel() );
		}
	}
}
