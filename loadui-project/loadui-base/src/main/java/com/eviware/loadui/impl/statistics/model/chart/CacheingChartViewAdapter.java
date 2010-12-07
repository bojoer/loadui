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
package com.eviware.loadui.impl.statistics.model.chart;

import java.util.concurrent.Callable;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.ChartViewAdapter;
import com.eviware.loadui.util.CacheMap;

/**
 * Wrapper for a ChartViewAdapter which caches objects using weak references.
 * 
 * @author dain.nilsson
 * 
 * @param <ChartViewType>
 */
@Deprecated
public class CacheingChartViewAdapter<ChartViewType extends ChartView> implements ChartViewAdapter<ChartViewType>
{
	private final ChartViewAdapter<ChartViewType> adapter;
	private final CacheMap<String, ChartViewType> cache = new CacheMap<String, ChartViewType>();

	public CacheingChartViewAdapter( ChartViewAdapter<ChartViewType> adapter )
	{
		this.adapter = adapter;
	}

	@Override
	public ChartViewType getChartView( final ChartGroup chartGroup )
	{
		// TODO: Fix key.
		return cache.getOrCreate( chartGroup.toString(), new Callable<ChartViewType>()
		{
			@Override
			public ChartViewType call() throws Exception
			{
				return adapter.getChartView( chartGroup );
			}
		} );
	}

	@Override
	public ChartViewType getChartView( final Chart chart )
	{
		// TODO: Fix key.
		return cache.getOrCreate( chart.toString(), new Callable<ChartViewType>()
		{
			@Override
			public ChartViewType call() throws Exception
			{
				return adapter.getChartView( chart );
			}
		} );
	}

	@Override
	public ChartViewType getChartView( final ChartGroup chartGroup, final String source )
	{
		// TODO: Fix key.
		return cache.getOrCreate( chartGroup.toString() + source, new Callable<ChartViewType>()
		{
			@Override
			public ChartViewType call() throws Exception
			{
				return adapter.getChartView( chartGroup, source );
			}
		} );
	}
}