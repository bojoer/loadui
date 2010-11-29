/*
 * Copyright 2010 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.statistics.model;

import java.util.HashMap;
import java.util.Map;

import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.ChartViewAdapter;
import com.eviware.loadui.api.statistics.model.chart.ChartViewAdapterRegistry;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.impl.statistics.model.chart.CacheingChartViewAdapter;
import com.eviware.loadui.impl.statistics.model.chart.LineChartViewAdapter;

/**
 * Simple implementation of ChartViewAdapterRegistry which provides
 * ChartViewAdapters for the included types.
 * 
 * @author dain.nilsson
 */
public class ChartViewAdapterRegistryImpl implements ChartViewAdapterRegistry
{
	private final Map<Class<? extends ChartView>, ChartViewAdapter<? extends ChartView>> adapters = new HashMap<Class<? extends ChartView>, ChartViewAdapter<? extends ChartView>>();

	public ChartViewAdapterRegistryImpl()
	{
		adapters.put( LineChartView.class, new CacheingChartViewAdapter<LineChartView>( new LineChartViewAdapter() ) );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <ChartViewType extends ChartView> ChartViewAdapter<ChartViewType> getAdapter( Class<ChartViewType> type )
	{
		return ( ChartViewAdapter<ChartViewType> )adapters.get( type );
	}
}