package com.eviware.loadui.ui.fx.views.analysis;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Function;

public class StatisticTab extends Tab
{
	private final Function<ChartGroup, ChartGroupView> CHART_GROUP_TO_VIEW = new Function<ChartGroup, ChartGroupView>()
	{
		@Override
		public ChartGroupView apply( ChartGroup chartGroup )
		{
			return new ChartGroupView( chartGroup, execution, poll );
		}
	};

	private final StatisticPage page;
	private final ObservableValue<Execution> execution;
	private final Observable poll;
	private final ObservableList<ChartGroupView> chartGroupViews;

	@FXML
	private VBox chartList;

	public StatisticTab( StatisticPage page, ObservableValue<Execution> execution, Observable poll )
	{
		this.page = page;
		this.execution = execution;
		this.poll = poll;
		chartGroupViews = ObservableLists.transform( ObservableLists.ofCollection( page ), CHART_GROUP_TO_VIEW );

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

		Bindings.bindContent( chartList.getChildren(), chartGroupViews );
	}
}