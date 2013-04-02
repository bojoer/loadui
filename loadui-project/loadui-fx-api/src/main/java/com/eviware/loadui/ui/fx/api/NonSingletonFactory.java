/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
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
