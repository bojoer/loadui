package com.eviware.loadui.ui.fx.views.assertions;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.util.statistics.StatisticNameFormatter;

import javafx.scene.control.TreeCell;

public class LabeledTreeCell extends TreeCell<Labeled>
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
			if( item instanceof Statistic<?> || item instanceof StatisticWrapper )
				setText( StatisticNameFormatter.format( item.getLabel() ) );
			else
				setText( item.getLabel() );
		}
	}
}
