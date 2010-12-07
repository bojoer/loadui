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
package com.eviware.loadui.api.statistics.model.chart;

import java.util.Collection;

import com.eviware.loadui.api.statistics.model.Chart;

/**
 * Holds ChartViews for a specific ChartGroup.
 * 
 * @author dain.nilsson
 * 
 * @param <ChartViewType>
 */
public interface ChartViewProvider<ChartViewType extends ChartView>
{
	/**
	 * Gets the ChartView for the ChartGroup.
	 * 
	 * @return
	 */
	public ChartViewType getChartViewForChartGroup();

	/**
	 * Gets the ChartView for a Chart.
	 * 
	 * @param chart
	 * @return
	 */
	public ChartViewType getChartViewForChart( Chart chart );

	/**
	 * Gets the ChartView for a source.
	 * 
	 * @param source
	 * @return
	 */
	public ChartViewType getChartViewForSource( String source );

	/**
	 * Gets the ChartViews for the Charts.
	 * 
	 * @return
	 */
	public Collection<ChartViewType> getChartViewsForCharts();

	/**
	 * Gets the ChartViews for the Sources.
	 * 
	 * @return
	 */
	public Collection<ChartViewType> getChartViewsForSources();

	/**
	 * Call when the ChartViewProvider is no longer needed to release any
	 * listeners it has registered.
	 */
	public void release();
}
