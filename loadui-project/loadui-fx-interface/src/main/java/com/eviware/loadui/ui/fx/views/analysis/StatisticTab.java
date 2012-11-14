package com.eviware.loadui.ui.fx.views.analysis;

import javafx.scene.control.Tab;

import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.ui.fx.util.Properties;

public class StatisticTab extends Tab
{
	private final StatisticPage page;

	public StatisticTab( StatisticPage page )
	{
		this.page = page;

		textProperty().bind( Properties.forLabel( page ) );
	}
}