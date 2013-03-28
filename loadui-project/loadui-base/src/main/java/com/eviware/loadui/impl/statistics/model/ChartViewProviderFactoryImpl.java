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
package com.eviware.loadui.impl.statistics.model;

import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.ChartViewProvider;
import com.eviware.loadui.api.statistics.model.chart.ChartViewProviderFactory;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.impl.statistics.model.chart.line.LineChartViewProvider;

public class ChartViewProviderFactoryImpl implements ChartViewProviderFactory
{
	private final Map<String, ProviderFactory<?>> factories = new HashMap<>();

	public ChartViewProviderFactoryImpl()
	{
		ProviderFactory<LineChartView> lineChartProviderFactory = new ProviderFactory<LineChartView>()
		{
			@Override
			public ChartViewProvider<LineChartView> create( ChartGroup chartGroup )
			{
				return new LineChartViewProvider( chartGroup );
			}
		};
		factories.put( LineChartView.class.getName(), lineChartProviderFactory );
		//For legacy reasons, to support older projects.
		factories.put( "com.eviware.loadui.api.statistics.model.chart.LineChartView", lineChartProviderFactory );
	}

	@Override
	public ChartViewProvider<?> buildProvider( String typeName, ChartGroup chartGroup )
	{
		return factories.get( typeName ).create( chartGroup );
	}

	private interface ProviderFactory<ChartViewType extends ChartView>
	{
		public ChartViewProvider<ChartViewType> create( ChartGroup chartGroup );
	}
}
