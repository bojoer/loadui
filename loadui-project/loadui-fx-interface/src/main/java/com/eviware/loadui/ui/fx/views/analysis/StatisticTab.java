package com.eviware.loadui.ui.fx.views.analysis;

import com.eviware.loadui.api.statistics.model.StatisticPage;

import javafx.scene.control.Tab;

public class StatisticTab extends Tab
{
	private final StatisticPage page;

	StatisticTab( StatisticPage page )
	{
		this.page = page;
		setText( page.getTitle() );
	}
}
