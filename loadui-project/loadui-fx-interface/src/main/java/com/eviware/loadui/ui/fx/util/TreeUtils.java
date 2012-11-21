package com.eviware.loadui.ui.fx.util;

import com.eviware.loadui.api.traits.Labeled;

import javafx.scene.control.TreeItem;

public class TreeUtils
{
	public static TreeItem<Labeled> dummyItem( final String label )
	{
		return new TreeItem<Labeled>( new Labeled()
		{
			@Override
			public String getLabel()
			{
				return label;
			}
		} );
	}
}
