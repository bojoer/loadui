package com.eviware.loadui.ui.fx.views.analysis;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;

public class StatisticTab extends Tab
{
	private final StatisticPage page;
	private final ObservableList<ChartGroup> chartGroups;

	@FXML
	private ListView<ChartGroup> chartList;

	public StatisticTab( StatisticPage page, Observable poll )
	{
		this.page = page;
		chartGroups = ObservableLists.ofCollection( page );

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
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

		Bindings.bindContent( chartList.getItems(), chartGroups );
	}
}