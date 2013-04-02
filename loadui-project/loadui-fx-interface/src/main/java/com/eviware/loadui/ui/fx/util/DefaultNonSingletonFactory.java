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
package com.eviware.loadui.ui.fx.util;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
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

	public ExecutionChart createExecutionChart( LineChartView lineChartView )
	{
		return new ScrollableLineChart( lineChartView );
	}

	@Override
	public ChartGroupView createChartGroupView( ChartGroup chartGroup, ObservableValue<Execution> currentExecution,
			Observable poll )
	{
		return new ChartGroupViewImpl( chartGroup, currentExecution, poll );
	}
}
