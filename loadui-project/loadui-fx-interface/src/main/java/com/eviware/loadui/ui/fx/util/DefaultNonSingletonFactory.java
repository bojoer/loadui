package com.eviware.loadui.ui.fx.util;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.NonSingletonFactory;
import com.eviware.loadui.ui.fx.api.analysis.ChartGroupView;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionChart;
import com.eviware.loadui.ui.fx.views.analysis.ChartGroupViewImpl;
import com.eviware.loadui.ui.fx.views.analysis.linechart.ScrollableLineChart;

public class DefaultNonSingletonFactory implements NonSingletonFactory
{

	private static final NonSingletonFactory instance = new DefaultNonSingletonFactory();

	public static final NonSingletonFactory get()
	{
		return instance;
	}

	private DefaultNonSingletonFactory()
	{
		// singleton
	}

	public ExecutionChart createExecutionChart()
	{
		return new ScrollableLineChart();
	}

	@Override
	public ChartGroupView createChartGroupView( ChartGroup chartGroup, ObservableValue<Execution> currentExecution,
			Observable poll )
	{
		return new ChartGroupViewImpl( chartGroup, currentExecution, poll );
	}
}
