package com.eviware.loadui.ui.fx.views.analysis;

import com.eviware.loadui.api.statistics.model.StatisticPage;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;

public class StatisticTab extends Tab
{
	StatisticTab( final StatisticPage page )
	{
		setText( page.getTitle() );
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
