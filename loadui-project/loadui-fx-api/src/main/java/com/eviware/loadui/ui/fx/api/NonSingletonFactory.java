package com.eviware.loadui.ui.fx.api;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.analysis.ChartGroupView;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionChart;

/**
 * Factory which can be used to provide non-singleton implementations of API
 * interfaces. The Factory itself may be a OSGi service, but the instances it
 * provides can come only from the bundle where the factory is declared.
 * 
 * @author renato
 * 
 */
public interface NonSingletonFactory
{
	/**
	 * Creates and returns an ExecutionChart
	 * 
	 * @return ExecutionChart
	 */
	ExecutionChart createExecutionChart( LineChartView linechartView );

	/**
	 * Creates and returns an ChartGroupView
	 * 
	 * @return ChartGroupView
	 */
	ChartGroupView createChartGroupView( ChartGroup chartGroup, ObservableValue<Execution> currentExecution,
			Observable poll );
}
