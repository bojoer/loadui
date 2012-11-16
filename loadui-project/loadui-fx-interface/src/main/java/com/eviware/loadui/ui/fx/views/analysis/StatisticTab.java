package com.eviware.loadui.ui.fx.views.analysis;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;

import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.ui.fx.util.Properties;

public class StatisticTab extends Tab
{
	StatisticTab( final StatisticPage page )
	{
		textProperty().bind( Properties.forLabel( page ) );
		setOnClosed( new EventHandler<Event>()
		{
			@Override
			public void handle( Event _ )
			{
				page.delete();
			}
		} );
	}
}
