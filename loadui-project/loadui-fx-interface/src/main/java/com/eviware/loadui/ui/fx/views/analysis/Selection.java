package com.eviware.loadui.ui.fx.views.analysis;

import javafx.scene.control.TreeItem;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.util.TreeUtils.LabeledStringValue;

@Immutable
public class Selection
{
	public final String source;
	public final String statistic;
	public final String variable;
	public final StatisticHolder holder;

	Selection( @Nonnull TreeItem<Labeled> selected, boolean selectedIsSource )
	{
		if( selectedIsSource )
		{
			source = ( ( LabeledStringValue )selected.getValue() ).getValue();
			statistic = selected.getParent().getValue().getLabel();
			variable = selected.getParent().getParent().getValue().getLabel();
			holder = ( StatisticHolder )selected.getParent().getParent().getParent().getValue();
		}
		else
		{
			source = null;
			statistic = selected.getValue().getLabel();
			variable = selected.getParent().getValue().getLabel();
			holder = ( StatisticHolder )selected.getParent().getParent().getValue();
		}
	}
}