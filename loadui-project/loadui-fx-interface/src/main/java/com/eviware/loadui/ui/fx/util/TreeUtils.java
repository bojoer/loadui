package com.eviware.loadui.ui.fx.util;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.traits.Labeled;

import javafx.scene.control.TreeItem;

public class TreeUtils
{
	public static TreeItem<Labeled> dummyItem( final String label )
	{
		return new TreeItem<Labeled>( new LabeledStringValue( label, label ) );
	}

	public static TreeItem<Labeled> dummyItem( final String label, @Nonnull final String value )
	{
		return new TreeItem<Labeled>( new LabeledStringValue( label, value ) );
	}

	public static class LabeledStringValue implements Labeled
	{
		private final String label;
		private final String value;

		public LabeledStringValue( @Nonnull final String label, final String value )
		{
			this.label = label;
			this.value = value;
		}

		@Override
		public String getLabel()
		{
			return label;
		}

		public String getValue()
		{
			return value;
		}
	}
}
