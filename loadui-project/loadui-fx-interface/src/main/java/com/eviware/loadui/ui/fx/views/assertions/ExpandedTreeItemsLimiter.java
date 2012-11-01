package com.eviware.loadui.ui.fx.views.assertions;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

import com.eviware.loadui.api.traits.Labeled;

final class ExpandedTreeItemsLimiter implements ChangeListener<Boolean>
{
	private final TreeItem<Labeled> variableItem;
	private final TreeItem<Labeled> holderItem;

	ExpandedTreeItemsLimiter( TreeItem<Labeled> variableItem, TreeItem<Labeled> holderItem )
	{
		this.variableItem = variableItem;
		this.holderItem = holderItem;
	}

	@Override
	public void changed( ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue )
	{
		if( newValue.booleanValue() )
		{
			for( TreeItem<Labeled> item : holderItem.getChildren() )
			{
				if( item != variableItem )
					item.setExpanded( false );
			}
		}
	}
}